package services.Impl;

import models.Answers;
import services.IAnswerService;

import java.util.List;

public class AnswerService implements IAnswerService {
    @Override
    public Answers answerQuestion(int questionID, String content) {
        return null;
    }

    @Override
    public List<Answers> getAnswersForQuestion(int questionID) {
        return List.of();
    }

    @Override
    public void upVoteAnswer(int answerID) {

    }

    @Override
    public List<Answers> getAnswersSortedByVotes(int questionID) {
        return List.of();
    }
}
