
package mods.invmod.common.entity;

import mods.invmod.common.nexus.INexusAccess;
import net.minecraft.world.World;


public class EntityIMImp extends EntityIMMob
{	
	public EntityIMImp(World world, INexusAccess nexus)
    {
        super(world, nexus);
        texture = "/mods/invmod/textures/imp.png";
        moveSpeed = 0.75F;
        attackStrength = 3;
        health = 11;
        setName("Imp");
        setGender(1);
        setJumpHeight(1);
        setCanClimb(true);
    }
	
	public EntityIMImp(World world)
    {
    	this(world, null);
    }
    
    
    // --------- Sparrow API --------- //

 	/**What should this entity be referred to as? (Dog, Cat, Human, Enderman, etc.)*/
 	@Override
	public String getSpecies()
 	{
 		return "Imp";
 	}
 	
 	@Override
	public int getTier()
 	{
		return 1;
	}

 	// ------------------------------- //
}
