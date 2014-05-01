/**
 * Copyright 2014 the staff of 52°North Initiative for Geospatial Open
 * Source Software GmbH in their free time
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spare.n52.yadarts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.impl.PlayerImpl;
import spare.n52.yadarts.event.EventListener;
import spare.n52.yadarts.games.AbstractGame;
import spare.n52.yadarts.games.GameEventAdapter;
import spare.n52.yadarts.games.GameEventBus;
import spare.n52.yadarts.games.GameStatusUpdateAdapter;
import spare.n52.yadarts.games.Score;
import spare.n52.yadarts.games.x01.GenericX01Game;

public class ConsoleLogger extends GameStatusUpdateAdapter implements EventListener {

	private static final Logger logger = LoggerFactory.getLogger(ConsoleLogger.class);
	private boolean ended;
	
	public ConsoleLogger() throws InitializationException, AlreadyRunningException {
		List<Player> players = new ArrayList<>();
		players.add(new PlayerImpl("Dietmar"));
		players.add(new PlayerImpl("Hugo"));
		
		
		EventEngine engine = EventEngine.instance();
		GenericX01Game x01Game = GenericX01Game.create(players, 301);
		x01Game.registerGameListener(this);
		
		engine.registerListener(x01Game);
		engine.registerListener(this);
		
		GameEventBus.instance().registerListener(new GameEventAdapter() {
			
			@Override
			public void onGameStarted(AbstractGame game) {
				logger.info("Game started!");
			}
			
		});
		
		GameEventBus.instance().startGame(x01Game);
		
		engine.start();
	}
	
	@Override
	public void onGameFinished(Map<Player, Score> playerScoreMap,
			List<Player> winner) {
		this.ended = true;
		synchronized (this) {
			this.notifyAll();	
		}
		logger.info("Game Finished!");
	}
	
	
	@Override
	public void receiveEvent(InteractionEvent event) {
		logger.info("Received Event: {}", event);
	}
	
	public static void main(String[] args) throws InitializationException, AlreadyRunningException, InterruptedException {
		ConsoleLogger cl = new ConsoleLogger();
		
		synchronized (cl) {
			while (!cl.ended) {
				cl.wait();				
			}
		}
	}

}
