package completablefuturestudy;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Stream;


// stolen from https://medium.com/@kalpads/fantastic-completablefuture-allof-and-how-to-handle-errors-27e8a97144a0
public class CompletableFutureAllOfAndExceptionallyStudy {
    public static void main(String[] args) {
        new CompletableFutureAllOfAndExceptionallyStudy().launch();
    }

    private void launch() {

//        String[] strings = {"EN", "ES", "SN", "EX"};
        String[] strings = {"EN", "ES"};
        CompletableFuture<Void> allFutures = null;
        CompletableFuture<Void> finalAllFutures = allFutures;
        allFutures = CompletableFuture
                .allOf(Stream.of(strings)
                        .map(this::getGreeting)
                        .toArray(CompletableFuture[]::new)).thenRun(()-> System.out.println("Done!!! " + Arrays.asList())
//                        .toArray(CompletableFuture[]::new)).thenAccept((x)-> System.out.println("Done:"  + x)
//                                x -> System.out.println("Got it:" + List.of(x))
                );
    }

    Logger log = Logger.getLogger("Test");
    ExecutorService executor = Executors.newCachedThreadPool();
//    List<String> langList = Arrays.asList("EN", "ES", "SN", "EX");

    private CompletableFuture<GreetHolder> getGreeting(String lang) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Task execution started." + Thread.currentThread());
                Thread.sleep(2000);
                log.info("Task execution stopped." + Thread.currentThread());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new GreetHolder(getGreet(lang));
        }, executor);
    }

//    List<CompletableFuture<GreetHolder>> completableFutures =
//            langList.stream().map(lang -> getGreeting(lang)).collect(Collectors.toList());

    private String getGreet(String lang) {
        switch (lang) {
            case "EN":
                return "Hello";
            case "ES":
                return "Hola";
            case "SN":
                return "Ayubovan";
            default:
                throw new IllegalArgumentException("Invalid lang param");
        }
    }

    private class GreetHolder {

        private String greet;

        public GreetHolder(String greet) {
            this.greet = greet;
        }

        public String getGreet() {
            return greet;
        }

        public void setGreet(String greet) {
            this.greet = greet;
        }
    }
}
