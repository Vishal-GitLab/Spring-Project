package com.fitness.activityservice.service;


import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor   // ye constructor generate karta hai
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserService userService;


    // ham object bhejenge iske through
    private final KafkaTemplate<String, Activity> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public  ActivityResponse trackActivity(ActivityRequest request) {   // track activity sidhe-sidhe activity ko save karne
        boolean isValidUser = userService.validateUser(request.getUserId());        if (!isValidUser) {
            throw new RuntimeException("Invalid User: " + request.getUserId());
        }
        Activity  activity  = Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();
        Activity savedActivity =  activityRepository.save(activity);
        log.info("Activity saved successfully with ID: {}",
                savedActivity.getId());


        try{
            kafkaTemplate.send(
                    topicName,
                    savedActivity.getUserId(),
                    savedActivity
            ).whenComplete((result, exception) -> {

                if (exception != null) {
                    log.error("Failed to send activity to Kafka", exception);
                } else {
                    log.info("Activity sent successfully to Kafka. Topic: {}, Offset: {}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapToResponse(savedActivity);        // mapToResponse -->> savedActivity ko map karega ActivityResponse me aur return kar dega 
     }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }

    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity> activityList = activityRepository.findByUserId(userId);
        return activityList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
