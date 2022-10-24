package com.tweetapp.serviceImpl;

import com.tweetapp.dao.TweetRepository;
import com.tweetapp.dao.UserRepository;
import com.tweetapp.daoImpl.TweetRepositoryImpl;
import com.tweetapp.daoImpl.UserRepositoryImpl;
import com.tweetapp.model.TweetDetails;
import com.tweetapp.model.UserDetails;
import com.tweetapp.service.TweetService;
import com.tweetapp.utils.TweetAppConstants;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TweetServiceImpl implements TweetService {
    
   
	TweetRepository tweetRepository=new TweetRepositoryImpl();

    @Override
    public String postATweet(TweetDetails tweet) {
        try{
            tweetRepository.postTweet(tweet);
            return TweetAppConstants.TWEET_ADDED_SUCCESSFULLY;
        }
        catch (Exception ex){
            System.out.println("Failed to add tweet!");
            return ex.getMessage();
        }

    }

    @Override
    public List<String> viewAllPosts() {
    	UserRepository userRepository=new UserRepositoryImpl();
    	List<String> users=userRepository.findAll().stream().filter(o->o.isStatus()).map(UserDetails::getUserName).collect(Collectors.toList());
        return tweetRepository.findAll().stream().filter(o->users.contains(o.getUsername())).map(TweetDetails::getTweet).collect(Collectors.toList());
    }

    @Override
    public List<String> viewTweetByUser(String username) {
        return tweetRepository.findAll().stream().filter(tweet->tweet.getUsername().equals(username))
                .map(TweetDetails::getTweet)
                .collect(Collectors.toList());
    }
	@Override
    public Map<String,List<String>> viewTweetByAllUser(){
    	return tweetRepository.findAll().stream().collect(Collectors.groupingBy(TweetDetails::getUsername,
    			Collectors.mapping(TweetDetails::getTweet, 
    					Collectors.toList())));
    }
}
