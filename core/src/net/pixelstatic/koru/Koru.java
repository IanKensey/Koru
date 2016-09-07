package net.pixelstatic.koru;

import net.pixelstatic.koru.modules.*;
import net.pixelstatic.koru.network.IClient;
import net.pixelstatic.koru.systems.InterpolationSystem;
import net.pixelstatic.koru.systems.KoruEngine;
import net.pixelstatic.koru.systems.RendererSystem;
import net.pixelstatic.koru.world.World;
import net.pixelstatic.utils.modules.ModuleController;

import com.badlogic.gdx.Gdx;

public class Koru extends ModuleController<Koru>{
	private IClient client;
	private static Koru instance;
	public KoruEngine engine;
	private static Thread thread;
	
	public Koru(IClient client){
		this.client = client;
	}

	@Override
	public void init(){
		instance = this;
		engine = new KoruEngine();
		thread = Thread.currentThread();
		
		addModule(Network.class);
		addModule(Renderer.class);
		addModule(Input.class);
		addModule(ClientData.class);
		addModule(World.class);
		
		getModule(Network.class).client = client;

		engine.addSystem(new RendererSystem(getModule(Renderer.class)));

		engine.addSystem(new InterpolationSystem());
	}
	
	@Override
	public void render(){
		try{
			engine.update(Gdx.graphics.getDeltaTime());
			super.render();
		}catch (Exception e){
			e.printStackTrace();
			Gdx.app.exit();
		}
	}

	public static void log(Object o){
		System.out.println(o);
		if(o instanceof Exception){
			((Exception)o).printStackTrace();
		}
	}

	public static Thread getThread(){
		return thread;
	}
	
	public static KoruEngine getEngine(){
		return instance.engine;
	}
}
