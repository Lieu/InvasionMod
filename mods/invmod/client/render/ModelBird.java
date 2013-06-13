package mods.invmod.client.render;

import java.util.ArrayList;
import java.util.List;

import mods.invmod.client.render.animation.InterpType;
import mods.invmod.client.render.animation.KeyFrame;
import mods.invmod.client.render.animation.ModelAnimator;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBird extends ModelBase
{
	private ModelAnimator animationWingFlap;
	private ModelRenderer body;
	private ModelRenderer rightwing1;
	private ModelRenderer leftwing1;
	private ModelRenderer head;
	private ModelRenderer beak;
	private ModelRenderer leftwing2;
	private ModelRenderer rightwing2;
	private ModelRenderer tail;
	private ModelRenderer legR;
	private ModelRenderer ltoeR;
	private ModelRenderer btoeR;
	private ModelRenderer rtoeR;
	private ModelRenderer thighR;
	private ModelRenderer legL;
	private ModelRenderer ltoeL;
	private ModelRenderer btoeL;
	private ModelRenderer rtoeL;
	private ModelRenderer thighL;

	public ModelBird()
	{
		textureWidth = 64;
		textureHeight = 32;

		body = new ModelRenderer(this, 24, 0);
		body.addBox(-3.5F, 0F, -3.5F, 7, 12, 7);
		body.setRotationPoint(3.5F, 7F, 3.5F);
		body.setTextureSize(64, 32);
		body.mirror = true;
		setRotation(body, 0F, 0F, 0F);
		rightwing1 = new ModelRenderer(this, 0, 22);
		rightwing1.addBox(-7F, -1F, -1F, 7, 9, 1);
		rightwing1.setRotationPoint(-3.5F, 2F, 3.5F);
		rightwing1.setTextureSize(64, 32);
		rightwing1.mirror = false;
		setRotation(rightwing1, 0F, 0F, 0F);
		rightwing2 = new ModelRenderer(this, 16, 24);
		rightwing2.addBox(-14F, -1F, -0.5F, 14, 7, 1);
		rightwing2.setRotationPoint(-7F, 0F, -0.5F);
		rightwing2.setTextureSize(64, 32);
		rightwing2.mirror = false;
		setRotation(rightwing2, 0F, 0F, 0F);
		rightwing1.addChild(rightwing2);
		leftwing1 = new ModelRenderer(this, 0, 22);
		leftwing1.addBox(0F, -1F, -1F, 7, 9, 1);
		leftwing1.setRotationPoint(3.5F, 2F, 3.5F);
		leftwing1.setTextureSize(64, 32);
		leftwing1.mirror = true;
		setRotation(leftwing1, 0F, 0F, 0F);
		leftwing2 = new ModelRenderer(this, 16, 24);
		leftwing2.addBox(0F, -1F, -0.5F, 14, 7, 1);
		leftwing2.setRotationPoint(7F, 0, -0.5F);
		leftwing2.setTextureSize(64, 32);
		leftwing2.mirror = true;
		setRotation(leftwing2, 0F, 0F, 0F);
		leftwing1.addChild(leftwing2);
		head = new ModelRenderer(this, 2, 0);
		head.addBox(-2.5F, -5F, -4F, 5, 6, 6);
		head.setRotationPoint(0F, 0.5F, 1.5F);
		head.setTextureSize(64, 32);
		head.mirror = true;
		setRotation(head, 0F, 0F, 0F);
		beak = new ModelRenderer(this, 19, 0);
		beak.addBox(-0.5F, 0F, -2F, 1, 2, 2);
		beak.setRotationPoint(0F, -3F, -4F);
		beak.setTextureSize(64, 32);
		beak.mirror = true;
		setRotation(beak, 0F, 0F, 0F);
		head.addChild(beak);
		tail = new ModelRenderer(this, 0, 12);
		tail.addBox(-3F, 0F, 0F, 5, 9, 1);
		tail.setRotationPoint(0.5F, 12F, 2.5F);
		tail.setTextureSize(64, 32);
		tail.mirror = true;
		setRotation(tail, 0.4461433F, 0F, 0F);
		legR = new ModelRenderer(this, 13, 12);
		legR.addBox(-0.5F, 0F, -0.5F, 1, 5, 1);
		legR.setRotationPoint(0, 0, 0);
		legR.setTextureSize(64, 32);
		legR.mirror = false;
		setRotation(legR, 0F, 0F, 0F);
		ltoeR = new ModelRenderer(this, 0, 0);
		ltoeR.addBox(0F, 0F, -2F, 1, 1, 2);
		ltoeR.setRotationPoint(0.2F, 4.0F, 0F);
		ltoeR.setTextureSize(64, 32);
		ltoeR.mirror = false;
		setRotation(ltoeR, 0F, -0.1396263F, 0F);
		legR.addChild(ltoeR);
		btoeR = new ModelRenderer(this, 0, 0);
		btoeR.addBox(-0.5F, 0F, 0F, 1, 1, 2);
		btoeR.setRotationPoint(0F, 4.0F, 0F);
		btoeR.setTextureSize(64, 32);
		btoeR.mirror = false;
		setRotation(btoeR, -0.3490659F, 0F, 0F);
		legR.addChild(btoeR);
		rtoeR = new ModelRenderer(this, 0, 0);
		rtoeR.addBox(-1F, 0F, -2F, 1, 1, 2);
		rtoeR.setRotationPoint(-0.2F, 4.0F, 0F);
		rtoeR.setTextureSize(64, 32);
		rtoeR.mirror = false;
		setRotation(rtoeR, 0F, 0.1396263F, 0F);
		legR.addChild(rtoeR);
		thighR = new ModelRenderer(this, 13, 18);
		thighR.addBox(-1F, 0F, -1F, 2, 2, 2);
		thighR.setRotationPoint(-1.5F, 12F, -1.0F);
		thighR.setTextureSize(64, 32);
		thighR.mirror = false;
		setRotation(thighR, 0F, 0F, 0F);
		thighR.addChild(legR);
		legL = new ModelRenderer(this, 13, 12);
		legL.addBox(-0.5F, 0F, -0.5F, 1, 5, 1);
		legL.setRotationPoint(0, 0, 0);
		legL.setTextureSize(64, 32);
		legL.mirror = true;
		setRotation(legL, 0F, 0F, 0F);
		ltoeL = new ModelRenderer(this, 0, 0);
		ltoeL.addBox(0F, 0F, -2F, 1, 1, 2);
		ltoeL.setRotationPoint(0.2F, 4.0F, 0F);
		ltoeL.setTextureSize(64, 32);
		ltoeL.mirror = true;
		setRotation(ltoeL, 0F, -0.1396263F, 0F);
		legL.addChild(ltoeL);
		btoeL = new ModelRenderer(this, 0, 0);
		btoeL.addBox(-0.5F, 0F, 0F, 1, 1, 2);
		btoeL.setRotationPoint(0F, 4.0F, 0F);
		btoeL.setTextureSize(64, 32);
		btoeL.mirror = true;
		setRotation(btoeL, -0.3490659F, 0F, 0F);
		legL.addChild(btoeL);
		rtoeL = new ModelRenderer(this, 0, 0);
		rtoeL.addBox(-1F, 0F, -2F, 1, 1, 2);
		rtoeL.setRotationPoint(-0.2F, 4F, 0F);
		rtoeL.setTextureSize(64, 32);
		rtoeL.mirror = true;
		setRotation(rtoeL, 0F, 0.1396263F, 0F);
		legL.addChild(rtoeL);
		thighL = new ModelRenderer(this, 13, 18);
		thighL.addBox(-1F, 0F, -1F, 2, 2, 2);
		thighL.setRotationPoint(1.5F, 12F, -1.0F);
		thighL.setTextureSize(64, 32);
		thighL.mirror = true;
		setRotation(thighL, 0F, 0F, 0F);
		thighL.addChild(legL);
		body.addChild(thighL);
		body.addChild(thighR);
		body.addChild(head);
		body.addChild(tail);
		body.addChild(rightwing1);
		body.addChild(leftwing1);
		
		animationWingFlap = new ModelAnimator();
		
		float frameUnit = 1.0F / 60.0F;
		List<KeyFrame> innerWingFrames = new ArrayList<KeyFrame>(12);
		innerWingFrames.add(new KeyFrame(0, 2F, -43.5F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(5 * frameUnit, 4, -38, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(10 * frameUnit, 5.5F, -27.5F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(15 * frameUnit, 5.5F, -7F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(20 * frameUnit, 5.5F, 15F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(25 * frameUnit, 4.5F, 30F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(30 * frameUnit, 2F, 38F, 9F, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(35 * frameUnit, 1F, 20F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(40 * frameUnit, 1F, 3.5F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(45 * frameUnit, 1F, -19F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(50 * frameUnit, -3F, -38F, 0, InterpType.LINEAR));
		innerWingFrames.add(new KeyFrame(55 * frameUnit, -1F, -48F, 0, InterpType.LINEAR));
		KeyFrame.toRadians(innerWingFrames);
		animationWingFlap.addPart(rightwing1, innerWingFrames);
		List<KeyFrame> copy = KeyFrame.cloneFrames(innerWingFrames);
		KeyFrame.mirrorFramesX(copy);
		animationWingFlap.addPart(leftwing1, copy);
		
		List<KeyFrame> outerWingFrames = new ArrayList<KeyFrame>(13);
		outerWingFrames.add(new KeyFrame(0, 2F, 34.5F, 0, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(5 * frameUnit, 5F, 13F, -7F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(10 * frameUnit, 7F, 8.5F, -10F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(15 * frameUnit, 7.5F, -2.5F, -10F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(25 * frameUnit, 5F, 7F, -10F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(30 * frameUnit, 2F, 15F, 0, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(35 * frameUnit, -3F, 37F, 12F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(40 * frameUnit, -9F, 56F, 27F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(45 * frameUnit, -13F, 68F, 28F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(50 * frameUnit, -13.5F, 70F, 31.5F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(53 * frameUnit, -9F, 71F, 31F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(55 * frameUnit, -3.5F, 65.5F, 22F, InterpType.LINEAR));
		outerWingFrames.add(new KeyFrame(58 * frameUnit, 0, 52F, 8F, InterpType.LINEAR));
		KeyFrame.toRadians(outerWingFrames);
		animationWingFlap.addPart(rightwing2, outerWingFrames);
		List<KeyFrame> copy2 = KeyFrame.cloneFrames(outerWingFrames);
		KeyFrame.mirrorFramesX(copy2);
		animationWingFlap.addPart(leftwing2, copy2);

	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		body.render(f5);
	}
	
	public void setFlyingAnimations(float flapProgress, float legSweepProgress, float roll)
	{
		// Set wing flapping animation flapPeriod = (0-1]
        animationWingFlap.updateAnimation(flapProgress);

        // Set roll
        body.rotateAngleY = body.rotateAngleX = 0;
        body.rotateAngleZ = -roll / 180F * (float)Math.PI;
        
        // Set how far the legs are tucked back (ie, for flight)
        thighR.rotateAngleX = 5F / 180F * (float)Math.PI * legSweepProgress;
        thighL.rotateAngleX = 5F / 180F * (float)Math.PI * legSweepProgress;
        legR.rotateAngleX = 5F / 180F * (float)Math.PI * legSweepProgress;
        legL.rotateAngleX = 5F / 180F * (float)Math.PI * legSweepProgress;
        ltoeR.rotateAngleX = 50F / 180F * (float)Math.PI * legSweepProgress;
        rtoeR.rotateAngleX = 50F / 180F * (float)Math.PI * legSweepProgress;
        btoeR.rotateAngleX = -20F / 180F * (float)Math.PI + (20F - 20F) * legSweepProgress;
        ltoeL.rotateAngleX = 50F / 180F * (float)Math.PI * legSweepProgress;
        rtoeL.rotateAngleX = 50F / 180F * (float)Math.PI * legSweepProgress;
        btoeL.rotateAngleX = -20F / 180F * (float)Math.PI + (20F / 180F * (float)Math.PI - 20F / 180F * (float)Math.PI) * legSweepProgress;
        
        // Set up/down reactionary body wobble for flapping motion
        body.rotationPointY = 7 + MathHelper.cos(flapProgress * (float)Math.PI * 2F) * 1.4F;
        thighR.rotateAngleX += MathHelper.cos(flapProgress * (float)Math.PI * 2F) * (5F / 180 * Math.PI);
        thighL.rotateAngleX += MathHelper.cos(flapProgress * (float)Math.PI * 2F) * (5F / 180 * Math.PI);
        tail.rotateAngleX = (float) (15F / 180 * Math.PI + MathHelper.cos(flapProgress * (float)Math.PI * 2F) * (2F / 180 * Math.PI));
        head.rotateAngleX = (float) (-18F / 180 * Math.PI - MathHelper.cos(flapProgress * (float)Math.PI * 2F) * (2F / 180 * Math.PI));
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float limbPeriod, float limbMaxMovement, float ticksExisted, float headYaw, float entityPitch, float unitScale, Entity entity)
	{
		super.setRotationAngles(limbPeriod, limbMaxMovement, ticksExisted, headYaw, entityPitch, unitScale, entity);
        body.rotateAngleX = ((float)Math.PI / 2) - entityPitch / 180F * (float)Math.PI; // + MathHelper.cos(ticksExisted * 0.1F) * 0.15F;
        
        
        
        
        
        //System.out.println(entityPitch);
        
        // Old Flapping
        //rightwing1.rotateAngleY = MathHelper.cos(ticksExisted * 0.6F) * (float)Math.PI * 0.25F;
        //leftwing1.rotateAngleY = -rightwing1.rotateAngleY;
        //rightwing2.rotateAngleY = rightwing1.rotateAngleY * 0.5F;
        //leftwing2.rotateAngleY = -rightwing1.rotateAngleY * 0.5F;
	}

}
