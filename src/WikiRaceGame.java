import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class WikiRaceGame {

    private WikiPageScrapper scraper;
    private LinkRanker linkRanker;
    private Player player;
    private String goalPage;

    public WikiRaceGame(String startPage, String goalPage) {
        this.scraper = new WikiPageScrapper();
        this.linkRanker = new LinkRanker();
        this.player = new Player(startPage);
        this.goalPage = goalPage;
        //System.out.println("start page: " + startPage + " and goal page: " + goalPage);
    }

    public void startGame() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Wikirace! Start: " + player.getCurrentPage() + ", Goal: " + goalPage);
        while (!player.getCurrentPage().equalsIgnoreCase(goalPage)) {
            System.out.println("\nYou are on the page: " + player.getCurrentPage());
            System.out.println("Fetching and ranking links...");

            try {
                List<String> links = scraper.getLinksFromPage(player.getCurrentPage());
                //System.out.println("Fetched links: " + links);

                List<String> rankedLinks = linkRanker.rankLinks(links, goalPage.replace("_", " "));
                //System.out.println("Ranked links: " + rankedLinks);

                displayLinks(rankedLinks);

                System.out.print("Choose a link by number: ");
                int choice = scanner.nextInt();
                if (choice >= 0 && choice < rankedLinks.size()) {
                    player.setCurrentPage(rankedLinks.get(rankedLinks.size() - choice));
                    player.incrementMoves();
                    System.out.println("Moved to page: " + player.getCurrentPage());
                } else {
                    System.out.println("Invalid choice, try again.");
                }

            } catch (IOException e) {
                System.out.println("Error fetching the page: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }

        System.out.println("\nCongratulations! You've reached the goal page in " + player.getNumberOfMoves() + " moves.");
    }

    private void displayLinks(List<String> links) {
        for (int i = 0; i < links.size(); i++) {
            System.out.println(links.size() - i + ": " + links.get(i));
        }
    }

    public static void main(String[] args) {
        Scanner scanner1 = new Scanner(System.in);
        System.out.print("Enter a Wikipedia page: ");
        String start = scanner1.nextLine();
        Scanner scanner2 = new Scanner(System.in);
        System.out.print("Enter the goal Wikipedia page: ");
        String goal = scanner2.nextLine();

        WikiRaceGame game = new WikiRaceGame(start, goal);
        game.startGame();
    }
}
