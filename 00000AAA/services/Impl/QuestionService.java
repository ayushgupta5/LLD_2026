package services.Impl;

import models.Questions;
import services.IQuestionService;

import java.util.List;

public class QuestionService implements IQuestionService {
    @Override
    public Questions askQuestion(String title, String description, String topics) {
        return null;
    }

    @Override
    public Questions getQuestion(int questionID) {
        return null;
    }

    @Override
    public List<Questions> getAllQuestions() {
        return List.of();
    }

    @Override
    public List<Questions> getQuestionsByTopic(String topicName) {
        return List.of();
    }

    @Override
    public void upvoteQuestion(int questionID) {

    }

    @Override
    public List<Questions> getQuestionsForUserFeed(String topics) {
        return List.of();
    }
}
