package sample;

//Tests the server using Clients
public class ClientTester {

    public static void main(String[] args) throws Exception {
        //We create 150 clients
        Client obj[] = new Client[150] ;

        //Connect each client to the server
        for(int x=0; x< obj.length; x++){
            obj[x] = new Client("localhost", 80,Integer.toString(x));
            Thread.sleep(50);
        }

        //Have them all spam the server
        while(true){
            for(int x=0; x<obj.length; x++){
                obj[x].writeToServer("I am " + x);
                Thread.sleep(5);
            }


        }
    }

}
