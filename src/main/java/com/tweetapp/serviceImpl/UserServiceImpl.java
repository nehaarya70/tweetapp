package com.tweetapp.serviceImpl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;

import com.tweetapp.dao.UserRepository;
import com.tweetapp.daoImpl.UserRepositoryImpl;
import com.tweetapp.exception.PasswordMismatchException;
import com.tweetapp.exception.UserExistsException;
import com.tweetapp.exception.UserNotFoundException;
import com.tweetapp.model.UserDetails;
import com.tweetapp.service.UserService;
import com.tweetapp.utils.TweetAppConstants;

public class UserServiceImpl implements UserService {

	UserRepository userRepository = new UserRepositoryImpl();

	@Override
	public String register(UserDetails userDetails) {
		System.out.println("Adding user, started . . .");
		boolean addUserCheck = false;
		try {
			if (null != userDetails && StringUtils.isNotBlank(userDetails.getFirstName())
					&& StringUtils.isNotBlank(userDetails.getUserName())
					&& StringUtils.isNotBlank(userDetails.getPassword())) {
				UserDetails user = userRepository.findbyId(userDetails.getUserName());
				if (user == null) {
					addUserCheck = userRepository.addUser(userDetails);
				} else {
					throw new UserExistsException(TweetAppConstants.USER_ALREADY_REGISTERED_LOGIN_NOW);
				}
			}
		} catch (Exception ex) {
			return ex.getMessage();
		}
		if (addUserCheck) {
			return TweetAppConstants.REGISTRATION_SUCCESSFUL_LOGIN_IN;
		}
		return "Technical Error! Not able to Register.";
	}

	@Override
	public boolean login(String username, String password) {
		System.out.println("Logging user, started . . .");
		try {
			UserDetails user = userRepository.findbyId(username);
			if (user == null) {
				throw new UserNotFoundException(TweetAppConstants.USERNAME_NOT_FOUND_RESISTER);
			}
			if (user.getUserName().equalsIgnoreCase(username) && user.getPassword().equals(password)) {
				user.setStatus(false);
				userRepository.updateStatus(user);
				System.out.println("Login Successful!");
				return true;
			}
			System.out.println("Password incorrect, try again!");
			return false;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}

	}

	@Override
	public boolean userExists(String username) {
		UserDetails user = userRepository.findbyId(username);
		if (user == null) {
			return false;
		}
		return true;
	}

	@Override
	public List<String> viewAllUsers() {
		return userRepository.findAll().stream().map(o -> o.getUserName()).collect(Collectors.toList());
	}

	@Override
	public boolean resetPassword(String username, String oldPassword, String newPassword) {
		System.out.println("Reset password, operation started . . .");
		try {
			UserDetails user = userRepository.findbyId(username);
			if (user == null) {
				throw new UserNotFoundException(TweetAppConstants.USERNAME_NOT_FOUND_RESISTER);
			}
			if (user.getPassword().equals(oldPassword)) {
				user.setPassword(newPassword);
				user.setStatus(true);
				userRepository.updateStatus(user);
				userRepository.updatePassword(user);
				System.out.println("Password reset Successful , Loggin again");
				return true;
			}
			throw new PasswordMismatchException(TweetAppConstants.OLD_PASSWORD_AND_NEW_PASSWORD_NOT_MATCHED);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}

	}

	@Override
	public boolean logout(String username) {
		System.out.println("Logging out, started . . .");
		try {
			UserDetails user = userRepository.findbyId(username);
			if (user == null) {
				throw new UserNotFoundException(TweetAppConstants.USERNAME_NOT_FOUND_RESISTER);
			}
			user.setStatus(true);
			userRepository.updateStatus(user);
			System.out.println("Logout Successfull!");
			return true;

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}

	@Override
	public boolean forgetPassword(String username, String newPassword) {

		ScriptEngineManager ee = new ScriptEngineManager();
		ScriptEngine e = ee.getEngineByName("Nashorn");
		try {
			UserDetails user = userRepository.findbyId(username);
			if (user == null) {
				throw new UserNotFoundException(TweetAppConstants.USERNAME_NOT_FOUND_RESISTER);
			}
			e.eval(new FileReader(
					"C:\\Local Eclipse Workspace\\tweetapp-component1\\src\\main\\java\\com\\tweetapp\\serviceImpl\\forgotPassword.js"));
			Invocable inv = (Invocable) e;
			boolean result = (boolean) inv.invokeFunction("forgotPassword", username, newPassword);
			if (result)
				System.out.println("Password Reset Successfull!");
			else
				System.out.println("Password Reset unsuccessfull!");
			return true;
		} catch (FileNotFoundException | ScriptException ex) {
			System.out.println(ex.getMessage());
			return false;
		} catch (NoSuchMethodException ex) {
			System.out.println(ex.getMessage());
			return false;
		} catch (UserNotFoundException e1) {
			System.out.println(e1.getMessage());
			return false;
		}
	}
}
