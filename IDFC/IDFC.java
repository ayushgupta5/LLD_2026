
class Fee {

}
// Strategy Pattern
interface FeeStrategy {
    void feeCalculation();
}
class Principal implements FeeStrategy {
    @Override
    public void feeCalculation() {}
}
class Interest implements FeeStrategy {
    @Override
    public void feeCalculation() {}
}
class Penality implements FeeStrategy {
    @Override
    public void feeCalculation() {}
}

class FeeProcessor {
    private FeeStrategy feeStrategy;
    public FeeProcessor(FeeStrategy feeStrategy) {
        this.feeStrategy = feeStrategy;
    }
    public void feeCalculation() {
        feeStrategy.feeCalculation();
    }
}
public class IDFC {
}
