import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.EncounterResult;
import com.pokegoapi.auth.GoogleAuthJson;
import com.pokegoapi.auth.GoogleAuthTokenJson;
import com.pokegoapi.auth.GoogleCredentialProvider;
import com.pokegoapi.auth.GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import okhttp3.OkHttpClient;

public class Start {

	public static void main(String[] args) {
		new Start();
	}

	private GraphicUI logger;
	private AbstractAction exit  = new AbstractAction(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			logger.log("exiting");
			for(Thread t : threads) t.interrupt();
			logger.save();
			System.exit(0);
			
		}
		
	};
	
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	
	private boolean googleAuth;
	
	private String ptcusername;
	private String ptcpassword;
	
	private String refreshToken = "";
	
	private double latitude;
	private double longitude;
	private double altitude = 0; //default altitude
	
	private PokemonGo pokemonGo;
	private OkHttpClient httpClient;
	
	private final String saveFile = "save.txt";
	
	private Thread walkerThread;
	private Runnable walk =  new Runnable(){

		@Override
		public void run() {
			
			
			while(true){
				
				//generera acceleration i latitud och longitud.
				double latacc = -0.000006 + (0.000012 * random.nextDouble());
				double lonacc = -0.000006 + (0.000012 * random.nextDouble());
				
				latOffset += latacc;
				lonOffset += lonacc;
				
				pokemonGo.setLocation(latitude + latOffset, longitude + lonOffset, altitude);
				
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
				
			}
			
		}
		
	};
	
	private double latOffset,lonOffset;
	
	private Random random;

	private Runnable pokeHunter = new Runnable(){

		@Override
		public void run() {
		
			try {
				
				List<CatchablePokemon> pokemons = pokemonGo.getMap().getCatchablePokemon();
				logger.log("" + pokemons.size());
				
				for(CatchablePokemon pokemon : pokemons){
					
					
					// You need to Encounter first.
					EncounterResult encResult = pokemon.encounterPokemon();
					// if encounter was succesful, catch
					if (encResult.wasSuccessful()) {
						logger.log("Encounted:" + pokemon.getPokemonId());
						CatchResult result = pokemon.catchPokemonWithRazzBerry();
						logger.log("Attempt to catch:" + pokemon.getPokemonId() + " " + result.getStatus());
					}
					
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			} catch (LoginFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			try {
				Thread.sleep(320);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
	};
	private Thread pokeHunterThread;
		
	public Start() {

		random = new Random();
		
		logger = new GraphicUI("PokeMeistro - botting for plebs!",exit);
				
		
		
		load(saveFile);
		
		try {
			Desktop.getDesktop().browse(new URI("https://www.google.se/maps/@" + latitude + "," + longitude + "15,25z"));
			
			int svar = JOptionPane.showConfirmDialog(null, "Is the dispalyed location correct?");
			if(svar == JOptionPane.YES_OPTION){
				logger.log("Coordinates confirmed, proceeding.");
			}else if(svar == JOptionPane.NO_OPTION){
				logger.log("Coordinates confirmed, exiting.");
				exit.actionPerformed(null);
			}else if(svar == JOptionPane.CANCEL_OPTION){
				exit.actionPerformed(null);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		httpClient = new OkHttpClient();
		if(googleAuth){
			
			pokemonGo = googleAuth();
			
		}else{
			pokemonGo = ptcAuth();
			
		}
		
		logger.log("seems like a working connection");
		
		walkerThread = new Thread(walk);
		threads.add(walkerThread);
		walkerThread.start();
		
		
		pokeHunterThread = new Thread(pokeHunter);
		threads.add(pokeHunterThread);
		pokeHunterThread.start();
		
		
		
		
	}
	
	private void load(String savefilename) {
	
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(savefilename));
			
			
			try{
				
				googleAuth = Boolean.parseBoolean(br.readLine().split(":")[1]); //1
				String refTok = br.readLine().split(":")[1];					//2
				if(refTok.equals("none")){
					refreshToken = "";
				}else{
					refreshToken = refTok;
				}
				latitude = Double.parseDouble(br.readLine().split(":")[1]);		//3
				longitude = Double.parseDouble(br.readLine().split(":")[1]);	//4
				ptcusername = br.readLine().split(":")[1];						//5
				ptcpassword = br.readLine().split(":")[1];						//6
				
			}catch(ArrayIndexOutOfBoundsException ex){
				
				System.err.println("Text part missing in the save file, please control the file.");
				
				
			}
			
			
			
			br.close();
			
		} catch (FileNotFoundException e) {
			
			logger.log("No save file found. Creating new default save file.");
			logger.log("Please write your information in the save file and restart the program.");
			
			//create default values file
			
			//assign default values
			
			googleAuth = true;
			latitude = 0.0;
			longitude = 0.0;
			ptcusername = "*";
			ptcpassword = "*";
			//write these values
			
			save(savefilename);
						
			System.exit(0);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void save(String savefilename){
		
		
		try {
			Files.deleteIfExists(new File(savefilename).toPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(savefilename));
			
			String googleAuthString;
				if(googleAuth) googleAuthString = "true"; else googleAuthString = "false";
			
			String latitudeString = "" + latitude;
			String longitudeString = "" + longitude;
			
			String refToken = "";
			
			if(refreshToken.equals("")){
				refToken = "none";
			}else{
				refToken = refreshToken;
			}
			
			bw.write("Use google authentication:" + googleAuthString  + "\n"); 	//1
			bw.write("Google refresh token (leave this line, program fixes this line automatically):" + refToken + "\n");//2
			bw.write("latitude to start on:" + latitudeString  + "\n");			//3
			bw.write("longitud to start on:" + longitudeString  + "\n");		//4
			bw.write("pokemon trainers club username:" + ptcusername + "\n");	//5
			bw.write("pokemon trainers club password:" + ptcpassword + "\n");	//6
			
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
	public PokemonGo ptcAuth(){
		logger.log("Authenticating with PTC.");
		PokemonGo pgo;
		
		try {
			pgo = new PokemonGo(new PtcCredentialProvider(httpClient, ptcusername, ptcpassword), httpClient);
		} catch (LoginFailedException e) {
			e.printStackTrace();
			logger.log("\n\n ERROR: couldn't login, wrong usn/pas or the ban hammer has spoken.");
			pgo = null;
			 
		} catch (RemoteServerException e) {
			e.printStackTrace();
			logger.log("\n\n ERROR: remote server exception.");
			pgo = null;
			 
		}
		return pgo;
	}

	public PokemonGo googleAuth(){
		logger.log("Authenticating with google.");
		PokemonGo pgo = null;
		
		GoogleCredentialProvider googleCreditentialProvider = null;
		
		if(!refreshToken.equals("")){
			
			try {
				googleCreditentialProvider = new GoogleCredentialProvider(httpClient, refreshToken);
			} catch (LoginFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				 
			} catch (RemoteServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				 
			}
			
		}else{
			
			OnGoogleLoginOAuthCompleteListener onGoogleLoginOAuthCompleteListener = new OnGoogleLoginOAuthCompleteListener() {
				
				@Override
				public void onTokenIdReceived(GoogleAuthTokenJson arg0) {
					refreshToken = arg0.getRefreshToken();
					save(saveFile);
				}
				
				@Override
				public void onInitialOAuthComplete(GoogleAuthJson arg0) {
					try {
						Desktop.getDesktop().browse(new URI(arg0.getVerificationUrl()));
						logger.log("Enter code:\t" + arg0.getUserCode());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			};
			
			try {
				googleCreditentialProvider = new GoogleCredentialProvider(httpClient, onGoogleLoginOAuthCompleteListener);
			} catch (LoginFailedException e) {
				e.printStackTrace();
				 
			}
		}

		try {
			pgo = new PokemonGo(googleCreditentialProvider, httpClient);
		} catch (LoginFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 
		} catch (RemoteServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 
		}
		
		return pgo;
	}
	
	private double coordsToMeters(float lat_a, float lng_a, float lat_b, float lng_b) {
	    float pk = (float) (180/3.14169);

	    float a1 = lat_a / pk;
	    float a2 = lng_a / pk;
	    float b1 = lat_b / pk;
	    float b2 = lng_b / pk;

	    double t1 = (Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2));
	    double t2 = (Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2));
	    double t3 = (Math.sin(a1)*Math.sin(b1));
	    double tt = Math.acos(t1 + t2 + t3);

	    return 6366000*tt;
	}
	
	
}

class GraphicUI extends JFrame{

	private JScrollPane pane;
	private JTextArea textArea;
	private JButton exitButton;
	
	public GraphicUI(String title,AbstractAction exitAction) {
		
		this.setTitle(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(640,480);
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		this.setBackground(Color.black);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setForeground(Color.GREEN);
		textArea.setBackground(Color.BLACK);
		
		pane = new JScrollPane(textArea);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.add(pane,BorderLayout.CENTER);
		
		exitButton = new JButton("exit");
		exitButton.addActionListener(exitAction);
		this.add(exitButton,BorderLayout.SOUTH);
		
		this.setVisible(true);
		
	}
	
	public void log(String text){
		textArea.append("["+Instant.now().toString()+"]" +text + "\n");
	}
	
	public void setTitleText(String text){
		this.setTitle(text);
	}
	
	public void save(){
		
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter("log " + Instant.now().toString()));
			bw.write(textArea.getText());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	}
