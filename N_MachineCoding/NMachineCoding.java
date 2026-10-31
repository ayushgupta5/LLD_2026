import java.util.HashMap;
import java.util.List;

/*
Cricbuzz
FR:-
1.LiveScore();
2. LiveCommentry();
3. PlayerStats();
4. MatchInfo();

1. Team
2. Player
4. Tournament
5 Match
6 venue
7. Innings
8. Score

mapping
Match--> venue
     --> TeamA + TeamB
     --> TournamentID
     --> ScoreID
     --> InningsID


19.3 --> 200/3 --> 19.4-> 4 Runs --> 200+4=204/3
19.5 --> W -->

*/
interface IScoreService{
    String liveScore(String matchID);
    Score updateScore(String scoreID, Score score, String action);
}
interface IScoreRepository {
    Score findScoreByMatchID(String matchID);
}
class ScoreRepository implements IScoreRepository {

    HashMap<String, Score> scoreDB = new HashMap<>();

    public ScoreRepository() {
        this.scoreDB = scoreDB;
    }

    @Override
    public Score findScoreByMatchID(String matchID) {
        return scoreDB.getOrDefault(matchID, null);
    }
}
class ScoreService implements IScoreService {

    @Override
    public String liveScore(String matchID) {
        IScoreRepository scoreRepository = new ScoreRepository();
        return scoreRepository.findScoreByMatchID(matchID);
    }
    public Score updateScore(String scoreID, Score score, String action) {

    }
}

interface PaymentStrategy {
    void pay(double amount);
}
class CASH implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Cash Payment is Going On");
    }
}

class UPI implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("UPI Payment is Going On");
    }
}

class CREDIT_CARD implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("CreditCard Payment is Going On");
    }
}

class PaymentProcessor {
    private PaymentStrategy paymentStrategy;
    void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    void pay(double amount) {
        paymentStrategy.pay(amount);
    }
}


interface ILiveCommentaryService {
    String liveCommentary(String matchID);
}
class LiveCommentaryService implements ILiveCommentaryService {

    @Override
    public String liveCommentary(String matchID) {
        return "";
    }
}

interface IPlayerStatsService {
    String PlayerStats(String playerID);
}
class PlayerStatsService implements IPlayerStatsService {

    @Override
    public String PlayerStats(String playerID) {
        return "";
    }
}

interface IMatchService {
    Match MatchInfo(String matchID);
}
class MatchService implements IMatchService {

    @Override
    public Match MatchInfo(String matchID) {
        return null;
    }
}

class Venue {
    private String venueID;
    private String venueName;
    private String location;
    private String city;

    public String getVenueID() {
        return venueID;
    }

    public void setVenueID(String venueID) {
        this.venueID = venueID;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Venue(String venueID, String venueName, String location, String city) {
        this.venueID = venueID;
        this.venueName = venueName;
        this.location = location;
        this.city = city;
    }
}
class Player {
    private String playerID;
    private String playerName;
    private String playerAge;
    private String playerStats;

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerAge() {
        return playerAge;
    }

    public void setPlayerAge(String playerAge) {
        this.playerAge = playerAge;
    }

    public String getPlayerStats() {
        return playerStats;
    }

    public void setPlayerStats(String playerStats) {
        this.playerStats = playerStats;
    }

    public Player(String playerID, String playerName, String playerAge, String playerStats) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.playerAge = playerAge;
        this.playerStats = playerStats;
    }
}
class Team {
    private String teamID;
    private List<Player> players;

    public String getTeamID() {
        return teamID;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Team(String teamID, List<Player> players) {
        this.teamID = teamID;
        this.players = players;
    }
}
class Score {
    private String scoreID;
    private String scoreStats;

    public String getScoreID() {
        return scoreID;
    }

    public void setScoreID(String scoreID) {
        this.scoreID = scoreID;
    }

    public String getScoreStats() {
        return scoreStats;
    }

    public void setScoreStats(String scoreStats) {
        this.scoreStats = scoreStats;
    }

    public Score(String scoreID, String scoreStats) {
        this.scoreID = scoreID;
        this.scoreStats = scoreStats;
    }
}
class Innings {
    private String inningsID;
    private String inningsInfo;
    private Team battingTeam;
    private Team bowlingTeam;

    public String getInningsID() {
        return inningsID;
    }

    public void setInningsID(String inningsID) {
        this.inningsID = inningsID;
    }

    public String getInningsInfo() {
        return inningsInfo;
    }

    public void setInningsInfo(String inningsInfo) {
        this.inningsInfo = inningsInfo;
    }

    public Team getBattingTeam() {
        return battingTeam;
    }

    public void setBattingTeam(Team battingTeam) {
        this.battingTeam = battingTeam;
    }

    public Team getBowlingTeam() {
        return bowlingTeam;
    }

    public void setBowlingTeam(Team bowlingTeam) {
        this.bowlingTeam = bowlingTeam;
    }

    public Innings(String inningsID, String inningsInfo, Team battingTeam, Team bowlingTeam) {
        this.inningsID = inningsID;
        this.inningsInfo = inningsInfo;
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
    }
}
class Tournament {
    private String tournamentID;
    private String tournamentName;
    private List<Team> teams;

    public String getTournamentID() {
        return tournamentID;
    }

    public void setTournamentID(String tournamentID) {
        this.tournamentID = tournamentID;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public Tournament(String tournamentID, String tournamentName, List<Team> teams) {
        this.tournamentID = tournamentID;
        this.tournamentName = tournamentName;
        this.teams = teams;
    }
}
class Commentary {
    private String commentaryID;
    private String commentaryStats;

    public Commentary(String commentaryID, String commentaryStats) {
        this.commentaryID = commentaryID;
        this.commentaryStats = commentaryStats;
    }

    public String getCommentaryID() {
        return commentaryID;
    }

    public void setCommentaryID(String commentaryID) {
        this.commentaryID = commentaryID;
    }

    public String getCommentaryStats() {
        return commentaryStats;
    }

    public void setCommentaryStats(String commentaryStats) {
        this.commentaryStats = commentaryStats;
    }
}
class Match {
    private String matchID;
    private Team teamA;
    private Team teamB;
    private Venue venue;
    private Score score;
    private Commentary commentary;
    private List<Innings> innings;

    public Match(String matchID, Team teamA, Team teamB, Venue venue, Score score, Commentary commentary, List<Innings> innings) {
        this.matchID = matchID;
        this.teamA = teamA;
        this.teamB = teamB;
        this.venue = venue;
        this.score = score;
        this.commentary = commentary;
        this.innings = innings;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public Team getTeamA() {
        return teamA;
    }

    public void setTeamA(Team teamA) {
        this.teamA = teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public void setTeamB(Team teamB) {
        this.teamB = teamB;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Commentary getCommentary() {
        return commentary;
    }

    public void setCommentary(Commentary commentary) {
        this.commentary = commentary;
    }

    public List<Innings> getInnings() {
        return innings;
    }

    public void setInnings(List<Innings> innings) {
        this.innings = innings;
    }
}
public class NMachineCoding {
    public static void main(String[] args) {

    }
}
