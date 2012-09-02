package Messaging;

import GlobalData.CTANetwork;
import System.PeopleCTA;
import com.rabbitmq.client.AMQP.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
//import com.rabbitmq.client.ConnectionParameters;
import com.rabbitmq.client.QueueingConsumer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A static class to manage send and receive from the queue. It extends thread
 * so that receive can be done in a loop. This class handles the rabbitMQ
 * message queue for the given CTA
 */
public class QueueManager extends Thread implements Serializable {

    /**
     * The rabbitMQ queue manager object
     */
    private static QueueManager queueManager = null;
    /**
     * The parameters for the queues to be created
     */
    private QueueParameters queueParameters = null;
    /**
     * The rabbitMQ queue receiver object
     */
    private QueueUser queueUser = null;
    /**
     * A hash map of the list of rabbitMQ channels corresponding the hostname
     * Each hostname is a host running CTA and these channels are used to send
     * messages to them. This map represents a directory / address map
     */
    private HashMap<String, Channel> hostChannelMap = null;
    /**
     * Flag to check to set up a new queue listener
     */
    public boolean setupQueueListener = false;

    /**
     * Create a new queue manager given the QueueParameters
     *
     * @param queueParameters the QueueParameters for the queue manager
     * @see QueueManager
     * @see QueueParameters
     */
    private QueueManager(QueueParameters queueParameters) {
        this.queueParameters = queueParameters;
    }

    /**
     * Get an instance of the QueueManager
     *
     * @param queueParameters the parameters for the QueueManager
     * @param queueUser the QueueUser for the QueueManager
     * @return returns an instance of QueueManager
     * @see QueueManager
     */
    public static QueueManager getInstance(QueueParameters queueParameters, QueueUser queueUser) {
        if (queueManager == null) {
            queueManager = new QueueManager(queueParameters);
        }
        queueManager.queueUser = queueUser;
        return queueManager;
    }

    /**
     * The run method addes a listener which listens to received messages in a
     * seperate theread
     */
    @Override
    public void run() {
        addQueueListener(queueParameters);
    }

    /**
     * This method creates a connection to the rabbitMQ server on each of the
     * given hosts and opens channels for communication. These channels are used
     * for communication between between the given CTA and the rest of the CTAs.
     *
     * @throws Exception
     */
    private void createConnectionAndChannel() throws Exception {

        Utilities.Log.logger.info("Creating a connection and channel");

        List<String> hosts = CTANetwork.hosts;
        Map<String, QueueParameters> hostQueueParamMap = CTANetwork.hostQueueMap;

        Utilities.Log.logger.info("Size of hosts: " + hosts.size() + " and host queue map: " + hostQueueParamMap.size());

        hostChannelMap = new HashMap<String, Channel>();

        for (int i = 0; i < hosts.size(); i++) {
            String host = hosts.get(i);
            QueueParameters hostQueueParameters = hostQueueParamMap.get(host);
            //ConnectionParameters params = new ConnectionParameters();
            //params.setUsername(hostQueueParameters.username);
            //params.setPassword(hostQueueParameters.password);
            //params.setVirtualHost(hostQueueParameters.virtualHost);
            //params.setRequestedHeartbeat(0);
            //ConnectionFactory factory = new ConnectionFactory(params);
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(13000);
            factory.setUsername(hostQueueParameters.username);
            factory.setPassword(hostQueueParameters.password);
            factory.setVirtualHost(hostQueueParameters.virtualHost);
            factory.setRequestedHeartbeat(0);
            //Connection conn = null;
            //conn= factory.newConnection();
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.exchangeDeclare(hostQueueParameters.exchange, "direct");
            //channel.queueDeclarePassive(hostQueueParameters.queueName);
            //channel.queueDeclare();
            channel.queueDeclare(hostQueueParameters.queueName,false,false,true,null);
            channel.queueBind(hostQueueParameters.queueName, hostQueueParameters.exchange, hostQueueParameters.routingKey);

            hostChannelMap.put(host, channel);
        }
        setupQueueListener = true;
        Utilities.Log.logger.info("Finished creating connection and channel");
        Utilities.Log.logger.info("Contents of Host Channel Map: " + hostChannelMap.toString());
    }

    /**
     * This method is called during CTA shutdown for stopping the messaging and
     * running cleanup on the message queues and channels
     *
     * @throws IOException
     */
    public void exitMessaging() throws IOException {
        List<String> hosts = CTANetwork.hosts;
        for (int i = 0; i < hosts.size(); i++) {
            String host = hosts.get(i);
            Channel channel = hostChannelMap.get(host);
            Connection conn = channel.getConnection();
            channel.close();
            conn.close();
        }
    }

    /**
     * This method adds a new queue listener given the queue parameters. The
     * listener waits for a message to arrive and calls the appropriate method
     * for processing, after it arrives
     *
     * @param queueParameters the QueueParameters for listening
     * @see QueueParameters
     */
    private void addQueueListener(QueueParameters queueParameters) {

        Utilities.Log.logger.info("Adding queue listener");

        try {
            createConnectionAndChannel();

            Utilities.Log.logger.info("started listening to input queue");

//            ConnectionParameters params = new ConnectionParameters();
//            params.setUsername(queueParameters.username);
//            params.setPassword(queueParameters.password);
//            params.setVirtualHost(queueParameters.virtualHost);
//            params.setRequestedHeartbeat(0);
//            ConnectionFactory factory = new ConnectionFactory(params);
//            Connection conn = null;
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(GlobalData.Constants.localHost);
            factory.setPort(13000);
            factory.setUsername(queueParameters.username);
            factory.setPassword(queueParameters.password);
            factory.setVirtualHost(queueParameters.virtualHost);
            factory.setRequestedHeartbeat(0);

            Connection conn = factory.newConnection();
            //conn = factory.newConnection(GlobalData.Constants.localHost);
            Channel channel = conn.createChannel();
            channel.exchangeDeclare(queueParameters.exchange, "direct");
            //channel.queueDeclare(queueParameters.queueName);
            //channel.queueDeclare();
            channel.queueDeclare(queueParameters.queueName,false,false,true,null);
            channel.queueBind(queueParameters.queueName, queueParameters.exchange, queueParameters.routingKey);

            //byte[] messageBodyBytes = "Hello, worldoooo!".getBytes();
            //channel.basicPublish(queueParameters.exchange, queueParameters.routingKey, null, messageBodyBytes);


            boolean noAck = false;
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueParameters.queueName, noAck, consumer);

            //AMQP.Queue.Purge(1, queueParameters.queueName, true);

            while (!noAck) {
//                Utilities.Log.logger.info("QM: I am listening for messages in the while loop 1");
                QueueingConsumer.Delivery delivery;
//                Utilities.Log.logger.info("QM: I am listening for messages in the while loop 2");
                try {
//                    Utilities.Log.logger.info("QM: I am listening for messages in the while loop 3");
                    delivery = consumer.nextDelivery();
//                    Utilities.Log.logger.info("QM: I am listening for messages in the while loop 4");
                } catch (InterruptedException ie) {
//                    Utilities.Log.logger.info("QM: I am listening for messages in the while loop 5");
                    continue;

                }

//                Utilities.Log.logger.info("QM: I am listening for messages in the while loop 6");
                //String message = new String(delivery.getBody());
                InputStream inputStream = new ByteArrayInputStream(delivery.getBody());
                ObjectInputStream input = new ObjectInputStream(inputStream);
                Message message = (Message) input.readObject();
//                Utilities.Log.logger.info("QM: I am listening for messages in the while loop 7");
                inputStream.close();
//                Utilities.Log.logger.info("QM: I am listening for messages in the while loop 8");
                input.close();
//                Utilities.Log.logger.info("QM: I am listening for messages in the while loop 9");
                //System.out.println("Received Message" + message);
                Utilities.Log.logger.info("Received Message");

                queueUser.processMessage.message = message;
                queueUser.processMessage.run();
//                Utilities.Log.logger.info("QM: I am listening for messages in the while loop 10");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                Utilities.Log.logger.info("QM: I am listening for messages in the while loop 11");
            }
            Utilities.Log.logger.info("Finished Adding queue listener");

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called when a message has to be sent form one CTA to
     * another. The method makes use of the already open channels between the
     * CTAs to send its messages.
     *
     * @param host the destination host name
     * @param message the message that has to be sent
     * @return true if the message was successfully sent
     * @see Message
     */
    public boolean send(String host, Message message) {
        //Utilities.Log.logger.info("RabbitMQ Send Method");

        while (setupQueueListener == false) {
            Utilities.Log.logger.info("Waiting to send");
        }

        QueueParameters hostQueueParameters = CTANetwork.hostQueueMap.get(host);

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

        //System.out.println(" send 1");

        try {

            //Utilities.Log.logger.info("Contents of Host Channel Map: " + hostChannelMap.toString());

            Channel channel = hostChannelMap.get(host);

            ObjectOutputStream outputWriter = new ObjectOutputStream(outputBuffer);
            outputWriter.writeObject(message);
            outputWriter.close(); //write to buffer and flush;

            byte[] messageBodyBytes = outputBuffer.toByteArray();
            channel.basicPublish(hostQueueParameters.exchange, hostQueueParameters.routingKey, null, messageBodyBytes);
            outputBuffer.close();
            return true;

        } catch (IOException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
            Utilities.Log.logger.info("Error Sending Message" + ex.getMessage());
            return false;
        }
    }

    /**
     * This method checks if two QueueManager objects are same
     *
     * @param obj an object of QueueManager type
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueueManager other = (QueueManager) obj;
        if (this.queueParameters != other.queueParameters && (this.queueParameters == null || !this.queueParameters.equals(other.queueParameters))) {
            return false;
        }
        return true;
    }

    /**
     * Returns the hash code of the QueueManager object
     *
     * @return the hash code of the QueueManager object
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.queueParameters != null ? this.queueParameters.hashCode() : 0);
        return hash;
    }

    /**
     * This is a Debug Main method used to ping different servers
     *
     * @param args UNSUED
     */
    public static void main(String[] args) {
        QueueParameters queueParameters = new QueueParameters("queue1", "guest", "guest", "/", "77", "exchangeName", "routingkey");
        CTANetwork.hostQueueMap.put("192.168.0.124", queueParameters);

        PeopleCTA peopleCTA = new PeopleCTA();

        QueueManager qm = QueueManager.getInstance(queueParameters, peopleCTA);
        //qm.send("192.168.0.124", "192.168.0.124 - gud morng");
    }
}
