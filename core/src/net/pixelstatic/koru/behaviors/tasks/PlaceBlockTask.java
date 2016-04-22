package net.pixelstatic.koru.behaviors.tasks;

import java.awt.Point;

import net.pixelstatic.koru.components.InventoryComponent;
import net.pixelstatic.koru.items.ItemStack;
import net.pixelstatic.koru.modules.World;
import net.pixelstatic.koru.server.KoruUpdater;
import net.pixelstatic.koru.world.Material;
import net.pixelstatic.koru.world.MaterialType;

import com.badlogic.gdx.math.Vector2;

public class PlaceBlockTask extends Task{
	final int x, y;
	final Material material;
	private boolean waited = false;

	public PlaceBlockTask(int x, int y, Material material){
		this.x = x;
		this.y = y;
		this.material = material;
	}

	@Override
	protected void update(){
		World world = KoruUpdater.instance.world;
		if(!World.inBounds(x, y)){
			finish();
			entity.log("Block index out of bounds.");
			return;
		}
	//	entity.group().reserveBlock(blockx, blocky);
		InventoryComponent inventory = entity.mapComponent(InventoryComponent.class);
		
		boolean missing = false;
		for(ItemStack stack : material.getDrops()){
			if( !inventory.hasItem(stack)){
				insertTask(new HarvestResourceTask(stack.item, stack.amount*2));
				missing = true;
			}
		}
		if(missing) return;
		
		int blockx = x, blocky = y;
		if(material.solid()){
			Point point = world.findEmptySpace(entity, x, y, entity.group());
			blockx = point.x;
			blocky = point.y;
		}
		int dist = 2;
		if(world.tiles[x][y].block.solid() && material.getType().tile()){
			dist = 11;
		}
		if(Vector2.dst(entity.getX(), entity.getY(), blockx * 12 + 6, (blocky) * 12 + 6) > dist){
			insertTask(new MoveTowardTask(blockx, blocky, dist));
			return;
		}else if(world.tiles[x][y].block != Material.air && 
				(!material.getType().tile() || world.tiles[x][y].block.getType() == MaterialType.grass  
				|| world.tiles[x][y].block.getType() == MaterialType.tree)){
			insertTask(new BreakBlockTask(world.tiles[x][y].block, x, y));
			return;
		}
		
		if(!waited && material.solid()){
			this.insertTask(new WaitUntilEmptyTask(material, x, y));
			waited = true;
			return;
		}
			
		
		world.tiles[x][y].setMaterial(material);
		world.updateTile(x, y);
		inventory.removeAll(material.getDrops());
		entity.group().registerBlock(entity, material, x, y);
		entity.group().unreserveBlock(x, y);
		finish();
	}
}
