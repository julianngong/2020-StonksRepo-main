package uobspe.stonks;

public class Recipt {

    enum TransactionType{
        Buy,
        Sell,
        Short,
        Cover
    }

    final private String owner;
    final private String stockln;
    final private int numberOfStockInvolved;
    final private float valueAtTransaction;
    final private TransactionType transactionType;
    final private Boolean wasBoughtByPlayer;



    public Recipt(String owner, String stockln, int numberPurchased, float valueAtPurchase, TransactionType type, Boolean boughtByPlayer) {
        this.owner = owner;
        this.stockln = stockln;
        this.numberOfStockInvolved = numberPurchased;
        this.valueAtTransaction = valueAtPurchase;
        this.transactionType = type;
        this.wasBoughtByPlayer = boughtByPlayer;
    }
/*
    public int getNumberInvolved(){
        return numberOfStockInvolved;
    }

    public float getValueAtTransaction() {
        return valueAtTransaction;
    }

    public String getStockln() {
        return stockln;
    }
*/
    public Boolean isPlayerTransaction(){
        return wasBoughtByPlayer;
    }

    public String getOwner() {
        return owner;
    }

    public TransactionType getTransactionType(){return transactionType;}

    public String getAsHtmlTableRow(){
        String toReturn = "<tr>";

        toReturn += "<td>" + getOwner() + "</td>";
        toReturn += "<td>" + stockln + "</td>";
        toReturn += "<td>" + String.valueOf(numberOfStockInvolved) + "</td>";
        toReturn += "<td>" + getTransactionString() + "</td>";
        toReturn += "<td>" + String.valueOf(valueAtTransaction) + "</td>";

        return toReturn + "</tr>";
    }

    private String getTransactionString(){
        switch(this.transactionType){
            case Sell:
                return "Sale";
            case Buy:
                return "Purchase";
            case Short:
                return "Short";
            case Cover:
                return "Cover";
            default:
                return "Error type";
        }
    }
}