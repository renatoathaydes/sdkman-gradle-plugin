public class Foo {
    public static void main( String[] args ) {
        // use a JavaFX class just to prove this was compiled with the .sdkmanrc JDK (java=11.0.8.fx-zulu)
        System.out.println( "Can see JavaFX? " + javafx.application.Application.class );
    }
}
