package services;

import models.Questions;

import java.util.List;

public interface IQuestionService {
    Questions askQuestion(String title, String description, String topics);
    Questions getQuestion(int questionID);
    List<Questions> getAllQuestions();
    List<Questions> getQuestionsByTopic(String topicName);
    void upvoteQuestion(int questionID);
    List<Questions> getQuestionsForUserFeed(String topics);
}
