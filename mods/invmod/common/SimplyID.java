package mods.invmod.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.world.World;

//Called from other entities on their first spawn in order to get an individual ID
public class SimplyID {
	private static int nextSimplyID;
	private static Set<String> loadedIDs = new HashSet<String>();
	private static String loadedWorld = null;
	private static File file = null;
	private static PrintWriter writer = null;

	public static String getNextSimplyID(Entity par1Entity) {
		loadSession(par1Entity.worldObj);
		
		nextSimplyID = 0;

		for (int i = nextSimplyID;;) {
			// create ID and increment nextSimplyID
			String id = EntityList.getEntityString(par1Entity) + nextSimplyID++; 
			if (loadedIDs.add(id)){
				writeIDToFile(id);
				return id;
			}
		}
	}
	
	public static void loadSession(World worldObj){
		if (loadedWorld == null || !worldObj.getSaveHandler().getWorldDirectoryName().equals(loadedWorld)){
			resetSimplyIDTo(worldObj);
		}
	}
	
	public static void resetSimplyIDTo(World world){
		//cancel out of the previous world first.
		if (writer != null){
			writer.flush();
			writer.close();
		}
		loadedIDs.clear();
		
		//set up new world.
		loadedWorld = world.getSaveHandler().getWorldDirectoryName();
		String directory = "saves/" + loadedWorld + "/";
		file = new File(directory + "savedIDs.txt");
		
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		} catch (FileNotFoundException e) {} catch (IOException e) {
			e.printStackTrace();
		}
		
		populateSet();
	}

	public static void writeIDToFile(String id){
		writer.println(id);
		writer.flush();
	}
	
	public static void populateSet(){
		FileReader pre = null;
		BufferedReader reader = null;
		try {
			pre = new FileReader(file);
			reader = new BufferedReader(pre);
			
			String line = null;
			
			try {
				while ((line = reader.readLine()) != null){
					if (line.startsWith("delete ")){
						deleteID(line, false);
					}
					else{
						addID(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if (reader != null){
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {}
		
		if (reader != null){
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		refreshLoadedIDFile();
	}
	
	private static void refreshLoadedIDFile(){
		try {
			PrintWriter writer = new PrintWriter(file);
			
			for (String id: loadedIDs) {
				writer.println(id);
			}
			
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/*public static Entity getEntityFromSimplyID(String par1String,
			Entity par1entity) {
		List list = par1entity.worldObj.getLoadedEntityList();
		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity) list.get(i);
			if (entity1 instanceof SparrowAPI
					&& ((SparrowAPI) entity1).getSimplyID().equals(par1String)) {
				if (!entity1.isDead) {
					return entity1;
				} else {
					return null;
				}
			}
		}
		return null;
	}*/

	public static Set<String> getLoadedIDs() {
		return loadedIDs;
	}

	public static void setLoadedIDs(Set<String> loadedIDs) {
		SimplyID.loadedIDs = loadedIDs;
	}
	
	public static void addID(String newID){
		loadedIDs.add(newID);
	}
	
	/**Removes ID from Set, called from within SimplyID*/
	public static void deleteID(String deletedID, Boolean flag){
		if (!flag && deletedID.startsWith("delete ")){
			deletedID = deletedID.split(" ")[1];
		}
		
		if (flag){
			writeIDToFile("delete " + deletedID);
		}
		
		loadedIDs.remove(deletedID);
	}
	
	/**Removes ID from Set, called from outside SimplyID (generally, when an entity is removed)*/
	public static void deleteID(World world, String string){
		loadSession(world);
		
		deleteID(string, true);
	}
	
	
}
