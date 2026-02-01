package services;

import models.Answers;

import java.util.List;

public interface IAnswerService {
    Answers answerQuestion(int questionID, String content);
    List<Answers> getAnswersForQuestion(int questionID);
    void upVoteAnswer(int answerID);
    List<Answers> getAnswersSortedByVotes(int questionID);
}
