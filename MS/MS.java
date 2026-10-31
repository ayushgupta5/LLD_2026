import java.util.*;


enum ModelType {
    GPT, CLAUDE, PERPLEXITY, GEMINI
}

class AIRequest {
    private int reqID;
    private String instruction;
    private ModelType modelType;

    public AIRequest(int reqID, String instruction, ModelType modelType) {
        this.reqID = reqID;
        this.instruction = instruction;
        this.modelType = modelType;
    }

    public int getReqID() {
        return reqID;
    }

    public void setReqID(int reqID) {
        this.reqID = reqID;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }
}

class AIResponse {
    private int reqID;
    private String updatedCode;
    private String explaination;
    private List<CodeChange> codeChanges;

    public AIResponse(int reqID, String updatedCode, String explaination, List<CodeChange> codeChanges) {
        this.reqID = reqID;
        this.updatedCode = updatedCode;
        this.explaination = explaination;
        this.codeChanges = codeChanges;
    }

    public int getReqID() {
        return reqID;
    }

    public void setReqID(int reqID) {
        this.reqID = reqID;
    }

    public String getUpdatedCode() {
        return updatedCode;
    }

    public void setUpdatedCode(String updatedCode) {
        this.updatedCode = updatedCode;
    }

    public String getExplaination() {
        return explaination;
    }

    public void setExplaination(String explaination) {
        this.explaination = explaination;
    }

    public List<CodeChange> getCodeChanges() {
        return codeChanges;
    }

    public void setCodeChanges(List<CodeChange> codeChanges) {
        this.codeChanges = codeChanges;
    }
}

class CodeChange {
    private int reqID;
    private int startLine;
    private int endLine;
    private String newCode;

    public CodeChange(int reqID, int startLine, int endLine, String newCode) {
        this.reqID = reqID;
        this.startLine = startLine;
        this.endLine = endLine;
        this.newCode = newCode;
    }

    public int getReqID() {
        return reqID;
    }

    public void setReqID(int reqID) {
        this.reqID = reqID;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getNewCode() {
        return newCode;
    }

    public void setNewCode(String newCode) {
        this.newCode = newCode;
    }
}

class Notification {
    private int notificationID;
    private String message;
    private User user;
    private String notificationType;
    private String templateID;
}

// Strategy Pattern for Choosing ModelType
interface SelectAIStrategy {
    AIResponse process(AIRequest aiRequest);
}

class GPT implements SelectAIStrategy {

    @Override
    public AIResponse process(AIRequest aiRequest) {
        System.out.println("Code is Refactored by GPT");
        return null;
    }
}

class Claude implements SelectAIStrategy {

    @Override
    public AIResponse process(AIRequest aiRequest) {
        System.out.println("Code is Refactored by Claude");
        return null;
    }
}

class Gemini implements SelectAIStrategy {

    @Override
    public AIResponse process(AIRequest aiRequest) {
        System.out.println("Code is Refactored by Gemini");
        return null;
    }
}

class Perplexity implements SelectAIStrategy {

    @Override
    public AIResponse process(AIRequest aiRequest) {
        System.out.println("Code is Refactored by Perplexity");
        return null;
    }
}

class ModelProcessor {
    private SelectAIStrategy selectAIStrategy;

    void setSelectAIStrategy(SelectAIStrategy selectAIStrategy) {
        this.selectAIStrategy = selectAIStrategy;
    }

    void process(AIRequest aiRequest) {
        selectAIStrategy.process(aiRequest);
    }
}

// Observer Pattern for Notification
interface Observer {
    void update(String message);
}

class User implements Observer {
    private int userID;
    private String name;
    private String email;

    public User(int userID, String name, String email) {
        this.userID = userID;
        this.name = name;
        this.email = email;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void update(String instruction) {
        System.out.println("Response is Generated for this Message : " + instruction + " for This User: " + name);
    }
}

interface Subject {
    void subscribe(Observer observer);

    void unsubscribe(Observer observer);

    void notify(String message);
}

class NotificationService implements Subject {

    List<Observer> observerList;

    public NotificationService(List<Observer> observerList) {
        this.observerList = observerList;
    }

    @Override
    public void subscribe(Observer observer) {
        observerList.add(observer);
    }

    @Override
    public void unsubscribe(Observer observer) {
        observerList.remove(observer);
    }

    @Override
    public void notify(String message) {
        for (Observer observer : observerList) {
            observer.update(message);
        }
    }
}

class AISelectionProcessor {
    SelectAIStrategy getModel(ModelType modelType) {
        if (modelType == ModelType.CLAUDE) {
            return new Claude();
        } else if (modelType == ModelType.GPT) {
            return new GPT();
        } else if (modelType == ModelType.PERPLEXITY) {
            return new Perplexity();
        } else if (modelType == ModelType.GEMINI) {
            return new Gemini();
        }
        return null;
    }
}

// Facade Pattern  (Adaptor)
class AIFacade {
    private SelectAIStrategy selectAIStrategy;

    public AIFacade() {
        this.selectAIStrategy = new GPT();
    }

    public void process(AIRequest aiRequest) {
        selectAIStrategy.process(aiRequest);
    }

}


public class MS {
    public static void main(String[] args) {
        ModelType modelType = ModelType.PERPLEXITY;
        AIRequest aiRequest = new AIRequest(1, "instruction", modelType);
        AISelectionProcessor aiSelectionProcessor = new AISelectionProcessor();
        SelectAIStrategy selectAIStrategy = aiSelectionProcessor.getModel(aiRequest.getModelType());
//        ModelProcessor modelProcessor;
//        modelProcessor.process(aiRequest);
//        ModelType modelType2 = ModelType.CLAUDE;
//        aiRequest.setModelType(modelType2);
//        modelProcessor.process(aiRequest);
        selectAIStrategy.process(aiRequest);


    }
}
