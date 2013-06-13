package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityIMBolt extends Entity implements IEntityAdditionalSpawnData
{
    private int age;
    private int ticksToRender;
    private long timeCreated;
    private double[][] vertices;
    private long lastVertexUpdate;
    private float yaw;
    private float pitch;
    private double distance;
    private float widthVariance;
    private float vecX;
    private float vecY;
    private float vecZ;
    private int soundMade;
    
	public EntityIMBolt(World world)
	{
		super(world);
		age = 0;
		timeCreated = lastVertexUpdate = System.currentTimeMillis();
		vertices = new double[3][0];
		widthVariance = 6.0F;
		ignoreFrustumCheck = true;
	}
	
	public EntityIMBolt(World world, double x, double y, double z)
	{
		this(world);
		setPosition(x, y, z);
	}
	
	public EntityIMBolt(World world, double x, double y, double z, double x2, double y2, double z2, int ticksToRender, int soundMade)
	{
		this(world, x, y, z);
		vecX = (float)(x2 - x);
		vecY = (float)(y2 - y);
		vecZ = (float)(z2 - z);
		this.ticksToRender= ticksToRender;
		this.soundMade = soundMade;
		setHeading(vecX, vecY, vecZ);
		doVertexUpdate();
	}
	
	@Override
	public void writeSpawnData(ByteArrayDataOutput data)
	{
		data.writeShort((short)ticksToRender);
		data.writeFloat((float)posX);
		data.writeFloat((float)posY);
		data.writeFloat((float)posZ);
		data.writeFloat((float)vecX);
		data.writeFloat((float)vecY);
		data.writeFloat((float)vecZ);
		data.writeByte((byte)soundMade);
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data)
	{
		ticksToRender = data.readShort();
		setPosition(data.readFloat(), data.readFloat(), data.readFloat());
		setHeading(data.readFloat(), data.readFloat(), data.readFloat());
		soundMade = data.readByte();
		doVertexUpdate();
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		age++;
		if(age == 1 && soundMade == 1)
			worldObj.playSoundAtEntity(this, "invsound.zap", 1.0F, 1.0F);
			
		if(age > ticksToRender)
			setDead();
	}
	
	// Could use a callback here but renderers should be stateless
	public double[][] getVertices()
	{
		long time = System.currentTimeMillis();
		if(time - timeCreated > ticksToRender * 50)
			return null;
		
		if(time - lastVertexUpdate >= 75)
		{
			doVertexUpdate();
			while(lastVertexUpdate + 50 <= time)
				lastVertexUpdate += 50;
		}
		
		return vertices;
	}
	
	public float getYaw()
	{
		return yaw;
	}
	
	public float getPitch()
	{
		return pitch;
	}
	
	@Override
	public void handleHealthUpdate(byte byte0)
    {
		if(byte0 == 0)
        {
			worldObj.playSoundAtEntity(this, "invsound.zap", 1.0F, 1.0F);
        }
        else if(byte0 == 1)
        {
        }
        else if(byte0 == 2)
        {
        }
    }
	
	@Override
	public void entityInit()
	{
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
	}

    @Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
	}
    
    private void setHeading(float x, float y, float z)
    {
    	float xzSq = x * x + z * z;
		yaw = ((float)(Math.atan2(x, z) * 180D / Math.PI) + 90);
		pitch = (float)(Math.atan2(MathHelper.sqrt_double(xzSq), y) * 180D / Math.PI);
		distance = Math.sqrt(xzSq + y * y);
    }
    
    private void doVertexUpdate()
    {
    	worldObj.theProfiler.startSection("IMBolt");
    	widthVariance = 10 / (float)Math.log10(distance + 1);
    	int numberOfVertexes = 60;
    	if(numberOfVertexes != vertices[0].length)
    	{
    		vertices[0] = new double[numberOfVertexes]; // x
    		vertices[1] = new double[numberOfVertexes]; // y
    		vertices[2] = new double[numberOfVertexes]; // z
    	}
    	
    	for(int vertex = 0; vertex < numberOfVertexes; vertex++)
    	{
    		vertices[1][vertex] = vertex * distance / (numberOfVertexes - 1);
    	}
    	
    	createSegment(0, numberOfVertexes - 1);
    	worldObj.theProfiler.endSection();
    }
    
    private void createSegment(int begin, int end)
    {
    	int points = end + 1 - begin;
    	if(points <= 4)
    	{
    		if(points == 3)
    		{
    			createVertex(begin, begin + 1, end);
    		}
    		else // if points == 4
    		{
    			createVertex(begin, begin + 1, end);
    			createVertex(begin, begin + 2, end);
    		}
    		return;
    	}
    	int midPoint = begin + points / 2;
    	createVertex(begin, midPoint, end);
    	createSegment(begin, midPoint);
    	createSegment(midPoint, end);
    }
    
    private void createVertex(int begin, int mid, int end)
    {
    	double difference = vertices[0][end] - vertices[0][begin]; // x difference
    	double yDiffToMid = (vertices[1][mid] - vertices[1][begin]);
    	double yRatio = yDiffToMid / (vertices[1][end] - vertices[1][begin]);
    	vertices[0][mid] = vertices[0][begin] + (difference * yRatio) + (worldObj.rand.nextFloat() - 0.5) * yDiffToMid * widthVariance;
    	difference = vertices[2][end] - vertices[2][begin]; // z difference
    	vertices[2][mid] = vertices[2][begin] + (difference * yRatio) + (worldObj.rand.nextFloat() - 0.5) * yDiffToMid * widthVariance;
    }
}
