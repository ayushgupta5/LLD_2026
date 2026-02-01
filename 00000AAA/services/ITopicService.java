package services;

import models.Topic;

import java.util.List;

public interface ITopicService {
    Topic createTopic(String topicName);
    Topic getTopic(String topicName);
    List<Topic> getAllTopics();

}
