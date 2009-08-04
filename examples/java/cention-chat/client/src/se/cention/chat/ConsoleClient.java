package se.cention.chat;

public class ConsoleClient {
	private CentionChatServiceStub stub;
	private int systemgroup;
	private int session;
	private int lastReceivedMessageTimestamp;
	
	public ConsoleClient( int g, String name, String email, String question ) throws Exception {
		systemgroup = g;
		stub = new CentionChatServiceStub("http://localhost/webframework/Skeleton/chat/interface/-/RPC/");
		
		CentionChatServiceStub.CentionChatUpdate update = createChat(name, email, question);
		
		if (update.getActive()) {
			session = update.getSession();
			
			printMessages(update.getMessages().getItem());
			
			java.lang.Thread thread = new java.lang.Thread() {
				public void run() {
					try {
						while (true) {
							sleep(10);
							CentionChatServiceStub.CentionChatUpdate update = getUpdate();
							printMessages(update.getMessages().getItem());
						}
					} catch( Exception e ) {
					}
				}
			};
			
			thread.start();
			
			java.io.Console console = System.console();
			String line;
			
			while (true) {
				line = console.readLine("> ");
				if( line.equals("/quit") ) {
					thread.stop();
					break;
				}
				thread.suspend();
				CentionChatServiceStub.CentionChatUpdate u = sendMessage(line);
				printMessages(u.getMessages().getItem());
				thread.resume();
			}
		}
	}
	
	private void printMessages( CentionChatServiceStub.CentionChatMessage[] messages ) {
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("hh:mm");
		
		for (int i = 0; i < messages.length; i++) {
			CentionChatServiceStub.CentionChatMessage message = messages[i];
			java.util.Date date = new java.util.Date(message.getTimestamp());
			
			System.out.println("[" + formatter.format(date) + "] <" + message.getSender() + "> " + message.getBody());
			
			if( message.getTimestamp() > lastReceivedMessageTimestamp ) {
				lastReceivedMessageTimestamp = message.getTimestamp();
			}
		}
	}
	
	private CentionChatServiceStub.CentionChatUpdate createChat(String name, String email, String question) throws Exception {
		CentionChatServiceStub.Create request = new CentionChatServiceStub.Create();
		request.setSystemgroup_id(systemgroup);
		request.setName(name);
		request.setEmail(email);
		request.setQuestion(question);
		CentionChatServiceStub.CreateResponse response = stub.create(request);
		return response.get_return();
	}
	
	private CentionChatServiceStub.CentionChatUpdate sendMessage(String message) throws Exception {
		java.util.Date date = new java.util.Date();
		CentionChatServiceStub.SendMessage request = new CentionChatServiceStub.SendMessage();
		request.setSession_id(session);
		request.setMessage(message);
		request.setTimestamp(lastReceivedMessageTimestamp);
		CentionChatServiceStub.SendMessageResponse response = stub.sendMessage(request);
		return response.get_return();
	}
	
	private CentionChatServiceStub.CentionChatUpdate getUpdate() throws Exception {
		CentionChatServiceStub.GetUpdate request = new CentionChatServiceStub.GetUpdate();
		request.setSession_id(session);
		request.setTimestamp(lastReceivedMessageTimestamp);
		CentionChatServiceStub.GetUpdateResponse response = stub.getUpdate(request);
		return response.get_return();
	}
	
	public static void main(String args[]) {
		try {
			ConsoleClient client = new ConsoleClient(1, "Tobias", "tobias@cention.se", "Hello World!");
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
}
