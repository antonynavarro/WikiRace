public class Player {

    private String currentPage;
    private int numberOfMoves;

    public Player(String startPage) {
        this.currentPage = startPage;
        this.numberOfMoves = 0;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public int getNumberOfMoves() {
        return numberOfMoves;
    }

    public void incrementMoves() {
        this.numberOfMoves++;
    }
}

