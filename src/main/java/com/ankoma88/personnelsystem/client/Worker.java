package com.ankoma88.personnelsystem.client;

import com.ankoma88.personnelsystem.model.Command;
import com.ankoma88.personnelsystem.model.Employee;
import com.ankoma88.personnelsystem.model.Message;
import com.ankoma88.personnelsystem.util.Settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class Worker {
    private static final Logger log = Logger.getLogger(Worker.class.getName());

    private ObjectOutputStream clientOutputStream;
    private ObjectInputStream clientInputStream;

    public  List<Employee> getSupervisors() {
        List<Employee> resultList = null;
        try {

            Message requestMessage = new Message(Command.GET_SUPERVISORS);

            openResources();
            clientOutputStream.writeObject(requestMessage);
            Message responseMessage = (Message) clientInputStream.readObject();
            Command command = responseMessage.getCommand();
            System.out.println("command result: " + command);

            resultList = responseMessage.getEmployees();

            closeResources(clientOutputStream, clientInputStream);

            return resultList;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return resultList;
    }


    public  List<Employee> getSubordinates(int key) {
        List<Employee> resultList = null;
        try {

            Message requestMessage = new Message(Command.GET_SUBORDINATES, key);

            openResources();

            clientOutputStream.writeObject(requestMessage);
            Message responseMessage = (Message) clientInputStream.readObject();
            Command command = responseMessage.getCommand();
            System.out.println("command result: " + command);

            resultList = responseMessage.getEmployees();

            closeResources(clientOutputStream, clientInputStream);

            return resultList;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return resultList;
    }

    public  Command deleteEmployee(int key) {
        Command resultCommand = null;
        try {

            Message requestMessage = new Message(Command.DELETE, key);

            openResources();

            clientOutputStream.writeObject(requestMessage);
            Message responseMessage = (Message) clientInputStream.readObject();
            Command command = responseMessage.getCommand();
            log.info("command result: " + command);

            resultCommand = responseMessage.getCommand();

            closeResources(clientOutputStream, clientInputStream);

        } catch (Exception e) {
            log.info(e.getMessage());
            return resultCommand;
        }
        return resultCommand;
    }

    public  Employee findEmployee(int key) {
        Employee resultEmployee = null;
        try {

            Message requestMessage = new Message(Command.READ, key);

            openResources();

            clientOutputStream.writeObject(requestMessage);
            Message responseMessage = (Message) clientInputStream.readObject();
            Command command = responseMessage.getCommand();
            log.info("command result: " + command);

            resultEmployee = responseMessage.getEmployee();

            closeResources(clientOutputStream, clientInputStream);

            return resultEmployee;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return resultEmployee;
    }

    public  Employee updateEmployee(Employee oldEmployee) {
        Employee resultEmployee = null;
        try {

            Message requestMessage = new Message(Command.UPDATE, oldEmployee);

            openResources();

            clientOutputStream.writeObject(requestMessage);
            Message responseMessage = (Message) clientInputStream.readObject();
            log.info("command result: " + responseMessage.getCommand());

            closeResources(clientOutputStream, clientInputStream);

            resultEmployee = responseMessage.getEmployee();
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return resultEmployee;
    }

    public  Employee create(Employee newEmployee) {
        Employee resultEmployee = null;
        try {

            Message requestMessage = new Message(Command.CREATE, newEmployee);

            openResources();

            clientOutputStream.writeObject(requestMessage);
            Message responseMessage = (Message) clientInputStream.readObject();
            Command command = responseMessage.getCommand();
            System.out.println("command result: " + command);

            resultEmployee = responseMessage.getEmployee();

            closeResources(clientOutputStream, clientInputStream);

            return resultEmployee;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return resultEmployee;
    }




        public void openResources() throws IOException {
            @SuppressWarnings("resource")
            Socket socketConnection = new Socket(Settings.IP, Settings.PORT);
            clientOutputStream = new ObjectOutputStream(socketConnection.getOutputStream());
            clientInputStream = new ObjectInputStream(socketConnection.getInputStream());

        }

    private void closeResources(ObjectOutputStream clientOutputStream, ObjectInputStream clientInputStream) throws IOException {
        clientOutputStream.close();
        clientInputStream.close();
    }

}
