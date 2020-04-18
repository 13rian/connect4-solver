package ch.wenkst.connect4.connect4_nply;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import ch.wenkst.connect4.connect4_nply.solver.SolveManager;
import ch.wenkst.sw_utils.logging.Log;

public class Main_CreateTranspositionTable {
	static {
		System.setProperty("config.file", "config/app.conf"); 			// application config file
	}
		
	private static Log log = Log.getLogger(Main_CreateTranspositionTable.class);
	private int nply = 8; 												// all positions with this number of moves played will be solved
	private int nplyTranspositions = 12; 								// nply transposition table that is used by the solver if present
	private SolveManager solveManager;
	

	public static void main(String[] args) throws InterruptedException {	
		Main_CreateTranspositionTable app = new Main_CreateTranspositionTable();
		
		// initialize the logger
		Log.initLogger(AppConfig.dirLoggerConfig);
		log.fine("starting connect4 " + app.nply + " ply solver");
		
		app.startApp();
	}

	
	private void startApp() {
		// get the configuration
		AppConfig.getInstance();

		// create all legal non-won connect4 positions, mirrored positions are taken out because they have the same score
		solveManager = new SolveManager(nply, nplyTranspositions);
		solveManager.createPositions();

		// solve the connect4 positions
		long startTime = System.currentTimeMillis();
		solveManager.solvePositions();
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		
		log.info("solver finished, total time needed " + elapsedTime / 1000 + "s");
	}
}
