package com.driver.services;


import com.driver.EntryDto.SubscriptionEntryDto;
import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.repository.SubscriptionRepository;
import com.driver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;


    @Autowired
    UserRepository userRepository;

    public Integer buySubscription(SubscriptionEntryDto subscriptionEntryDto){
        Subscription subscription = new Subscription();
        User user = userRepository.findById(subscriptionEntryDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set the user on the subscription
        subscription.setUser(user);

        subscription.setSubscriptionType(subscriptionEntryDto.getSubscriptionType());
        subscription.setNoOfScreensSubscribed(subscriptionEntryDto.getNoOfScreensRequired());

        // Calculate amount
        int amount = 0;
        switch(subscriptionEntryDto.getSubscriptionType()) {
            case BASIC:
                amount = subscriptionEntryDto.getNoOfScreensRequired() * 200 + 500;
                break;
            case PRO:
                amount = subscriptionEntryDto.getNoOfScreensRequired() * 250 + 800;
                break;
            case ELITE:
                amount = subscriptionEntryDto.getNoOfScreensRequired() * 350 + 1000;
                break;
        }
        subscription.setTotalAmountPaid(amount);

        // Save the subscription
        subscription = subscriptionRepository.save(subscription);

        // Update the user's subscription
        user.setSubscription(subscription);
        userRepository.save(user);

        return amount;
    }

    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository

        User user = userRepository.findById(userId).orElseThrow(() -> new Exception("User not found"));
        Subscription subscription = user.getSubscription();
        SubscriptionType currentType = subscription.getSubscriptionType();

        if (currentType == SubscriptionType.ELITE) {
            throw new Exception("Already the best Subscription");
        }

        int currentAmount = subscription.getTotalAmountPaid();
        int newAmount = 0;

        if (currentType == SubscriptionType.BASIC) {
            newAmount = 800 + (200 * subscription.getNoOfScreensSubscribed());
            subscription.setSubscriptionType(SubscriptionType.PRO);
        } else if (currentType == SubscriptionType.PRO) {
            newAmount = 1000 + (350 * subscription.getNoOfScreensSubscribed());
            subscription.setSubscriptionType(SubscriptionType.ELITE);
        }

        subscription.setTotalAmountPaid(newAmount);
        subscriptionRepository.save(subscription);

        return newAmount - currentAmount;

    }

    public Integer calculateTotalRevenueOfHotstar(){

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb

        List<Subscription> subscriptions = subscriptionRepository.findAll();
        int totalRevenue = 0;

        for (Subscription subscription : subscriptions) {
            totalRevenue += subscription.getTotalAmountPaid();
        }

        return totalRevenue;

    }

}
