import java.util.*;
import java.time.LocalDate;

enum JourneyState {
    CREATED, ACTIVE, PAUSED, EXPIRED
}

enum StageType {
    ONBOARDING, INTERMEDIATE, TERMINAL
}

enum JourneyType {
    TIME_BOUND, PERPETUAL
}

class Payload {
    String userId;
    Map<String, Object> params;

    Payload(String userId, Map<String, Object> params) {
        this.userId = userId;
        this.params = params;
    }
}

interface Condition {
    boolean evaluate(Map<String, Object> params);
}

interface JourneyObserver {
    void onStageChange(String userId, Stage stage);
}

class EqualsCondition implements Condition {
    String key;
    Object expected;

    EqualsCondition(String key, Object expected) {
        this.key = key;
        this.expected = expected;
    }

    public boolean evaluate(Map<String, Object> params) {
        return expected.equals(params.get(key));
    }
}

class RangeCondition implements Condition {
    String key;
    int min, max;

    RangeCondition(String key, int min, int max) {
        this.key = key;
        this.min = min;
        this.max = max;
    }

    public boolean evaluate(Map<String, Object> params) {
        int val = (int) params.getOrDefault(key, 0);
        return val >= min && val <= max;
    }
}

class Stage {
    String name;
    StageType type;
    boolean smsEnabled;
    List<Transition> transitions = new ArrayList<>();

    Stage(String name, StageType type) {
        this.name = name;
        this.type = type;
        this.smsEnabled = false;
    }
}

class Transition {
    Condition condition;
    Stage nextStage;

    Transition(Condition condition, Stage nextStage) {
        this.condition = condition;
        this.nextStage = nextStage;
    }
}

class Journey {
    String id;
    JourneyState state;
    JourneyType type;
    Stage startStage;
    LocalDate startDate;
    LocalDate endDate;

    Journey(String id, Stage startStage, JourneyType type, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.startStage = startStage;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = JourneyState.CREATED;
    }

    boolean canTransitionTo(JourneyState newState) {
        if (state == JourneyState.CREATED && newState == JourneyState.ACTIVE) return true;
        if (state == JourneyState.ACTIVE && newState == JourneyState.PAUSED) return true;
        if (state == JourneyState.ACTIVE && newState == JourneyState.EXPIRED) return true;
        if (state == JourneyState.PAUSED && newState == JourneyState.ACTIVE) return true;
        if (state == JourneyState.PAUSED && newState == JourneyState.EXPIRED) return true;
        return false;
    }

    boolean isExpired() {
        if (type == JourneyType.TIME_BOUND && endDate != null) {
            return LocalDate.now().isAfter(endDate);
        }
        return false;
    }
}

class UserProgress {
    String userId;
    String journeyId;
    Stage currentStage;

    UserProgress(String userId, String journeyId, Stage currentStage) {
        this.userId = userId;
        this.journeyId = journeyId;
        this.currentStage = currentStage;
    }
}

class SMSObserver implements JourneyObserver {
    public void onStageChange(String userId, Stage stage) {
        if (stage.smsEnabled) {
            System.out.println("SMS sent to " + userId + " for stage " + stage.name);
        }
    }
}

class JourneyException extends RuntimeException {
    String errorCode;

    JourneyException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

class JourneyService {
    Map<String, Journey> journeys = new HashMap<>();
    Map<String, UserProgress> userProgress = new HashMap<>();
    List<JourneyObserver> observers = new ArrayList<>();

    void subscribe(JourneyObserver observer) {
        observers.add(observer);
    }

    void createJourney(Journey journey) {
        if (journey == null || journey.id == null) {
            throw new JourneyException("INVALID_INPUT", "Journey or id cannot be null");
        }
        if (journeys.containsKey(journey.id)) {
            throw new JourneyException("JOURNEY_EXISTS", "Journey already exists: " + journey.id);
        }
        journeys.put(journey.id, journey);
    }

    Journey getJourney(String journeyId) {
        Journey journey = journeys.get(journeyId);
        if (journey == null) {
            throw new JourneyException("JOURNEY_NOT_FOUND", "Journey not found: " + journeyId);
        }
        return journey;
    }

    void updateState(String journeyId, JourneyState newState) {
        Journey journey = getJourney(journeyId);
        if (!journey.canTransitionTo(newState)) {
            throw new JourneyException("INVALID_TRANSITION", journey.state + " to " + newState + " not allowed");
        }
        journey.state = newState;
    }

    void evaluate(Payload payload) {
        if (payload == null || payload.userId == null) {
            throw new JourneyException("INVALID_PAYLOAD", "Payload or userId cannot be null");
        }

        for (Journey journey : journeys.values()) {
            if (journey.isExpired()) {
                journey.state = JourneyState.EXPIRED;
            }
            if (journey.state != JourneyState.ACTIVE) continue;

            String key = payload.userId + ":" + journey.id;
            UserProgress progress = userProgress.get(key);

            if (progress == null) {
                tryOnboard(payload.userId, journey, payload.params);
            } else if (progress.currentStage.type != StageType.TERMINAL) {
                tryTransition(progress, payload.params);
            }
        }
    }

    private void tryOnboard(String userId, Journey journey, Map<String, Object> params) {
        Stage start = journey.startStage;
        for (Transition t : start.transitions) {
            if (t.condition.evaluate(params)) {
                UserProgress newProgress = new UserProgress(userId, journey.id, t.nextStage);
                userProgress.put(userId + ":" + journey.id, newProgress);
                notifyObservers(userId, t.nextStage);
                return;
            }
        }
    }

    private void tryTransition(UserProgress progress, Map<String, Object> params) {
        for (Transition t : progress.currentStage.transitions) {
            if (t.condition.evaluate(params)) {
                progress.currentStage = t.nextStage;
                notifyObservers(progress.userId, t.nextStage);
                return;
            }
        }
    }

    Stage getCurrentStage(String userId, String journeyId) {
        UserProgress progress = userProgress.get(userId + ":" + journeyId);
        if (progress == null) {
            throw new JourneyException("USER_NOT_ONBOARDED", userId + " not onboarded to " + journeyId);
        }
        return progress.currentStage;
    }

    boolean isOnboarded(String userId, String journeyId) {
        return userProgress.containsKey(userId + ":" + journeyId);
    }

    private void notifyObservers(String userId, Stage stage) {
        for (JourneyObserver obs : observers) {
            obs.onStageChange(userId, stage);
        }
    }
}

public class PhonePeMachineCoding {
    public static void main(String[] args) {
        Stage onboarding = new Stage("ONBOARDING", StageType.ONBOARDING);
        Stage kycDone = new Stage("KYC_DONE", StageType.INTERMEDIATE);
        Stage completed = new Stage("COMPLETED", StageType.TERMINAL);

        kycDone.smsEnabled = true;
        completed.smsEnabled = true;

        onboarding.transitions.add(new Transition(new EqualsCondition("kyc", true), kycDone));
        kycDone.transitions.add(new Transition(new RangeCondition("amount", 500, 10000), completed));

        Journey journey = new Journey("J1", onboarding, JourneyType.PERPETUAL, null, null);

        JourneyService service = new JourneyService();
        service.subscribe(new SMSObserver());
        service.createJourney(journey);
        service.updateState("J1", JourneyState.ACTIVE);

        service.evaluate(new Payload("user1", Map.of("kyc", true)));
        System.out.println(service.isOnboarded("user1", "J1"));
        System.out.println(service.getCurrentStage("user1", "J1").name);

        service.evaluate(new Payload("user1", Map.of("amount", 1000)));
        System.out.println(service.getCurrentStage("user1", "J1").name);

        service.evaluate(new Payload("user2", Map.of("kyc", false)));
        System.out.println(service.isOnboarded("user2", "J1"));

        try {
            service.updateState("J1", JourneyState.CREATED);
        } catch (JourneyException e) {
            System.out.println(e.errorCode + ": " + e.getMessage());
        }

        try {
            service.getCurrentStage("user99", "J1");
        } catch (JourneyException e) {
            System.out.println(e.errorCode + ": " + e.getMessage());
        }

        try {
            service.createJourney(journey);
        } catch (JourneyException e) {
            System.out.println(e.errorCode + ": " + e.getMessage());
        }

        Stage ob2 = new Stage("START", StageType.ONBOARDING);
        Stage mid = new Stage("MIDDLE", StageType.INTERMEDIATE);
        ob2.transitions.add(new Transition(new EqualsCondition("plan", "premium"), mid));

        Journey timeBound = new Journey("J2", ob2, JourneyType.TIME_BOUND, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        service.createJourney(timeBound);
        service.updateState("J2", JourneyState.ACTIVE);

        service.evaluate(new Payload("user3", Map.of("plan", "premium")));
        System.out.println(service.isOnboarded("user3", "J2"));
    }
}
