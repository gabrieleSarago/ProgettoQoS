package standardNetwork;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class debugger implements Runnable {

    String fileString = "";
    private boolean status;
    boolean hasToRun;
    BufferedWriter bw;

    LinkedList<String> message;

    public debugger() {
        status = true;
        message = new LinkedList<String>();
        hasToRun = true;
    }

    public debugger(String fileName) {
        fileString = fileName;
        status = true;
        message = new LinkedList<String>();
        hasToRun = true;
        try {
            bw = new BufferedWriter(new FileWriter(fileString));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean getStatus() {
        return status;
    }

    public void addMessage(String m) {
        message.add(m);
    }

    public void stopDebugger() {
        hasToRun = false;
    }

    public void run() {
        while (hasToRun) {
            if (message.size() > 0) {
                String m = message.get(0);
                message.remove(0);

                if (!fileString.equals("")) {
                    try {
                        bw.write(m);
                        bw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(m);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(debugger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
