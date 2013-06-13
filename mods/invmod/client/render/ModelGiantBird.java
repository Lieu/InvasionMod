package mods.invmod.client.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import mods.invmod.client.render.animation.AnimationAction;
import mods.invmod.client.render.animation.AnimationState;
import mods.invmod.client.render.animation.BonesWings;
import mods.invmod.client.render.animation.InterpType;
import mods.invmod.client.render.animation.KeyFrame;
import mods.invmod.client.render.animation.ModelAnimator;
import mods.invmod.client.render.animation.BonesBirdLegs;
import mods.invmod.common.util.MathUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelGiantBird extends ModelBase
{
	private ModelAnimator animationFlap;
	private ModelAnimator animationRun;
	
	ModelRenderer body;
	ModelRenderer rightThigh;
	ModelRenderer rightLeg;
	ModelRenderer rightAnkle;
	ModelRenderer rightToeB;
	ModelRenderer rightClawB;
	ModelRenderer rightToeL;
	ModelRenderer rightClawL;
	ModelRenderer rightToeM;
	ModelRenderer rightClawM;
	ModelRenderer rightToeR;
	ModelRenderer rightClawR;
	ModelRenderer leftThigh;
	ModelRenderer leftLeg;
	ModelRenderer leftAnkle;
	ModelRenderer leftToeB;
	ModelRenderer leftClawB;
	ModelRenderer leftToeL;
	ModelRenderer leftClawL;
	ModelRenderer leftToeM;
	ModelRenderer leftClawM;
	ModelRenderer leftToeR;
	ModelRenderer leftClawR;
	ModelRenderer neck1;
	ModelRenderer neck2;
	ModelRenderer neck3;
	ModelRenderer head;
	ModelRenderer upperBeak;
	ModelRenderer upperBeakTip;
	ModelRenderer lowerBeak;
	ModelRenderer lowerBeakTip;
	ModelRenderer headFeathers;
	ModelRenderer backNeckFeathers;
	ModelRenderer leftNeckFeathers;
	ModelRenderer rightNeckFeathers;
	ModelRenderer leftWing1;
	ModelRenderer leftWing2;
	ModelRenderer leftWing3;
	ModelRenderer tail;
	ModelRenderer rightWing1;
	ModelRenderer rightWing2;
	ModelRenderer rightWing3;

	public ModelGiantBird()
	{
		this(0.0f);
	}

	public ModelGiantBird(float par1)
	{
		body = new ModelRenderer( this, 0, 0 );
		body.setTextureSize( 128, 128 );
		body.addBox( -10F, -10F, -10F, 20, 30, 20);
		body.setRotationPoint( 0F, -19F, 0F );
		rightThigh = new ModelRenderer( this, 84, 82 );
		rightThigh.setTextureSize( 128, 128 );
		rightThigh.addBox( -4.5F, -3.5F, -4.5F, 9, 15, 9);
		rightThigh.setRotationPoint( -5F, 20F, -2F );
		rightLeg = new ModelRenderer( this, 56, 50 );
		rightLeg.setTextureSize( 128, 128 );
		rightLeg.addBox( -2F, -3F, -2F, 4, 16, 4);
		rightLeg.setRotationPoint(0, 11, 0);
		rightAnkle = new ModelRenderer( this, 16, 16 );
		rightAnkle.setTextureSize( 128, 128 );
		rightAnkle.addBox( 0F, 0F, 0F, 0, 0, 0);
		rightAnkle.setRotationPoint(0, 12, 0);
		rightToeB = new ModelRenderer( this, 60, 0 );
		rightToeB.setTextureSize( 128, 128 );
		rightToeB.addBox( -1F, -1F, -1F, 2, 8, 2);
		rightToeB.setRotationPoint(0, 0, 2);
		rightClawB = new ModelRenderer( this, 0, 11 );
		rightClawB.setTextureSize( 128, 128 );
		rightClawB.addBox( -0.5F, 0F, -1F, 1, 4, 2);
		rightClawB.setRotationPoint(0, 6, 0);
		rightToeL = new ModelRenderer( this, 0, 0 );
		rightToeL.setTextureSize( 128, 128 );
		rightToeL.addBox( -1F, 0.5F, -1F, 2, 9, 2);
		rightToeL.setRotationPoint(-0.5F, 0, 1);
		rightClawL = new ModelRenderer( this, 0, 11 );
		rightClawL.setTextureSize( 128, 128 );
		rightClawL.addBox( -0.5F, 0F, -1F, 1, 4, 2);
		rightClawL.setRotationPoint(0, 9, 0);
		rightToeM = new ModelRenderer( this, 8, 0 );
		rightToeM.setTextureSize( 128, 128 );
		rightToeM.addBox( -1F, 0F, -1F, 2, 10, 2);
		rightToeM.setRotationPoint(0, 0, 0);
		rightClawM = new ModelRenderer( this, 0, 11 );
		rightClawM.setTextureSize( 128, 128 );
		rightClawM.addBox( -0.5F, 0F, -1F, 1, 4, 2);
		rightClawM.setRotationPoint(0, 9, 0);
		rightToeR = new ModelRenderer( this, 0, 0 );
		rightToeR.setTextureSize( 128, 128 );
		rightToeR.addBox( -1F, -0.5F, -1F, 2, 9, 2);
		rightToeR.setRotationPoint(1, 0, 1);
		rightClawR = new ModelRenderer( this, 0, 11 );
		rightClawR.setTextureSize( 128, 128 );
		rightClawR.addBox( -0.5F, 0F, -1F, 1, 4, 2);
		rightClawR.setRotationPoint(0, 8, 0);
		leftThigh = new ModelRenderer( this, 84, 82 );
		leftThigh.setTextureSize( 128, 128 );
		leftThigh.addBox( -4.5F, -3.5F, -4.5F, 9, 15, 9);
		leftThigh.setRotationPoint(5F, 20F, -2F);
		leftThigh.mirror = true;
		leftLeg = new ModelRenderer( this, 56, 50 );
		leftLeg.setTextureSize( 128, 128 );
		leftLeg.addBox( -2F, -3F, -2F, 4, 16, 4);
		leftLeg.setRotationPoint(0, 11, 0);
		leftLeg.mirror = true;
		leftAnkle = new ModelRenderer( this, 16, 16 );
		leftAnkle.setTextureSize( 128, 128 );
		leftAnkle.addBox( 0F, 0F, 0F, 0, 0, 0);
		leftAnkle.setRotationPoint(0, 12, 0);
		leftToeB = new ModelRenderer( this, 60, 0 );
		leftToeB.setTextureSize( 128, 128 );
		leftToeB.addBox( -1F, -1F, -1F, 2, 8, 2);
		leftToeB.setRotationPoint(0, 0, 2);
		leftClawB = new ModelRenderer( this, 0, 11 );
		leftClawB.setTextureSize( 128, 128 );
		leftClawB.addBox( -0.5F, 0F, -1F, 1, 4, 2);
		leftClawB.setRotationPoint(0, 6, 0);
		leftToeL = new ModelRenderer( this, 0, 0 );
		leftToeL.setTextureSize( 128, 128 );
		leftToeL.addBox( -1F, 0.5F, -1F, 2, 9, 2);
		leftToeL.setRotationPoint(0.5F, 0, 1F);
		leftClawL = new ModelRenderer( this, 0, 11 );
		leftClawL.setTextureSize( 128, 128 );
		leftClawL.addBox( -0.5F, 0F, -1F, 1, 4, 2);
		leftClawL.setRotationPoint(0, 9, 0);
		leftToeM = new ModelRenderer( this, 8, 0 );
		leftToeM.setTextureSize( 128, 128 );
		leftToeM.addBox( -1F, 0F, -1F, 2, 10, 2);
		leftToeM.setRotationPoint(0, 0, 0);
		leftClawM = new ModelRenderer( this, 0, 11 );
		leftClawM.setTextureSize( 128, 128 );
		leftClawM.addBox( -0.5F, 0F, -1F, 1, 4, 2);
		leftClawM.setRotationPoint(0, 9, 0);
		leftToeR = new ModelRenderer( this, 0, 0 );
		leftToeR.setTextureSize( 128, 128 );
		leftToeR.addBox( -1F, -0.5F, -1F, 2, 9, 2);
		leftToeR.setRotationPoint(-1, 0, 1);
		leftClawR = new ModelRenderer( this, 0, 11 );
		leftClawR.setTextureSize( 128, 128 );
		leftClawR.addBox( -0.5F, 0F, -1F, 1, 4, 2);
		leftClawR.setRotationPoint(0, 8, 0);
		neck1 = new ModelRenderer( this, 43, 95 );
		neck1.setTextureSize( 128, 128 );
		neck1.addBox( -7F, -7F, -6.5F, 14, 10, 13);
		neck1.setRotationPoint(0, -10, 1);
		neck2 = new ModelRenderer( this, 50, 73 );
		neck2.setTextureSize( 128, 128 );
		neck2.addBox( -5F, -4F, -5F, 10, 8, 10);
		neck2.setRotationPoint(0, -8, 0);
		neck3 = new ModelRenderer( this, 80, 65 );
		neck3.setTextureSize( 128, 128 );
		neck3.addBox( -4F, -5.5F, -5F, 8, 5, 10);
		neck3.setRotationPoint(0, -2, 0);
		head = new ModelRenderer( this, 14, 108 );
		head.setTextureSize( 128, 128 );
		head.addBox( -4.5F, -5F, -9.5F, 9, 8, 11);
		head.setRotationPoint(0, -4, 0);
		upperBeak = new ModelRenderer( this, 54, 118 );
		upperBeak.setTextureSize( 128, 128 );
		upperBeak.addBox( -2.5F, -1F, -3F, 5, 2, 6);
		upperBeak.setRotationPoint(0, -0.8F, -10F);
		upperBeakTip = new ModelRenderer( this, 70, 118 );
		upperBeakTip.setTextureSize( 128, 128 );
		upperBeakTip.addBox( -1F, -1F, -1F, 2, 2, 2);
		upperBeakTip.setRotationPoint(0, 0, -4);
		lowerBeak = new ModelRenderer( this, 78, 118 );
		lowerBeak.setTextureSize( 128, 128 );
		lowerBeak.addBox( -2.5F, -1F, -3F, 5, 2, 6);
		lowerBeak.setRotationPoint(0, 1.5F, -10F);
		lowerBeakTip = new ModelRenderer( this, 76, 121 );
		lowerBeakTip.setTextureSize( 128, 128 );
		lowerBeakTip.addBox( -1F, -0.5F, -1F, 2, 1, 2);
		lowerBeakTip.setRotationPoint(0, -0.5F, -4F);
		headFeathers = new ModelRenderer( this, -5, 121 );
		headFeathers.setTextureSize( 128, 128 );
		headFeathers.addBox( -3.5F, 0F, -0.5F, 7, 0, 5);
		headFeathers.setRotationPoint(0, -5, 0);
		backNeckFeathers = new ModelRenderer( this, -7, 108 );
		backNeckFeathers.setTextureSize( 128, 128 );
		backNeckFeathers.addBox( -4F, 0F, -1.5F, 8, 0, 7);
		backNeckFeathers.setRotationPoint(0, -3, 5);
		leftNeckFeathers = new ModelRenderer( this, -6, 115 );
		leftNeckFeathers.setTextureSize( 128, 128 );
		leftNeckFeathers.addBox( -3F, 0F, -1F, 6, 0, 6);
		leftNeckFeathers.setRotationPoint(4, -3, 2);
		rightNeckFeathers = new ModelRenderer( this, -6, 115 );
		rightNeckFeathers.setTextureSize( 128, 128 );
		rightNeckFeathers.addBox( -3F, 0F, -1F, 6, 0, 6);
		rightNeckFeathers.setRotationPoint(-4, -3, 2);
		leftWing1 = new ModelRenderer( this, 0, 50 );
		leftWing1.mirror = true;
		leftWing1.setTextureSize( 128, 128 );
		leftWing1.addBox( -0.5F, -4.5F, -1.5F, 25, 29, 3);
		leftWing1.setRotationPoint(7, -8, 6);
		leftWing2 = new ModelRenderer( this, 0, 82 );
		leftWing2.mirror = true;
		leftWing2.setTextureSize( 128, 128 );
		leftWing2.addBox( -2.5F, -5F, -1F, 23, 24, 2);
		leftWing2.setRotationPoint(23, 1, 0);
		leftWing3 = new ModelRenderer( this, 80, 0 );
		leftWing3.mirror = true;
		leftWing3.setTextureSize( 128, 128 );
		leftWing3.addBox( -2.5F, -5F, -0.5F, 23, 22, 1);
		leftWing3.setRotationPoint(21F, 0.2F, 0.3F);
		tail = new ModelRenderer( this, 80, 23 );
		tail.setTextureSize( 128, 128 );
		tail.addBox( -8.5F, -5F, -1F, 17, 40, 2);
		tail.setRotationPoint(0, 19, 8);
		rightWing1 = new ModelRenderer( this, 0, 50 );
		rightWing1.setTextureSize( 128, 128 );
		rightWing1.addBox( -24.5F, -4.5F, -1.5F, 25, 29, 3);
		rightWing1.setRotationPoint(-7, -8, 6);
		rightWing2 = new ModelRenderer( this, 0, 82 );
		rightWing2.setTextureSize( 128, 128 );
		rightWing2.addBox( -20.5F, -5F, -1F, 23, 24, 2);
		rightWing2.setRotationPoint(-23, 1, 0);
		rightWing3 = new ModelRenderer( this, 80, 0 );
		rightWing3.setTextureSize( 128, 128 );
		rightWing3.addBox( -20.5F, -5F, -0.5F, 23, 22, 1);
		rightWing3.setRotationPoint(-21F, 0.2F, 0.3F);
		
		rightToeB.addChild(rightClawB);
		rightToeR.addChild(rightClawR);
		rightToeM.addChild(rightClawM);
		rightToeL.addChild(rightClawL);
		leftToeB.addChild(leftClawB);
		leftToeR.addChild(leftClawR);
		leftToeM.addChild(leftClawM);
		leftToeL.addChild(leftClawL);
		rightAnkle.addChild(rightToeB);
		rightAnkle.addChild(rightToeR);
		rightAnkle.addChild(rightToeM);
		rightAnkle.addChild(rightToeL);
		leftAnkle.addChild(leftToeB);
		leftAnkle.addChild(leftToeR);
		leftAnkle.addChild(leftToeM);
		leftAnkle.addChild(leftToeL);
		rightLeg.addChild(rightAnkle);
		leftLeg.addChild(leftAnkle);
		rightThigh.addChild(rightLeg);
		leftThigh.addChild(leftLeg);
		upperBeak.addChild(upperBeakTip);
		lowerBeak.addChild(lowerBeakTip);
		head.addChild(upperBeak);
		head.addChild(lowerBeak);
		head.addChild(headFeathers);
		neck3.addChild(head);
		neck3.addChild(leftNeckFeathers);
		neck3.addChild(rightNeckFeathers);
		neck3.addChild(backNeckFeathers);
		neck2.addChild(neck3);
		neck1.addChild(neck2);
		leftWing2.addChild(leftWing3);
		leftWing1.addChild(leftWing2);
		rightWing2.addChild(rightWing3);
		rightWing1.addChild(rightWing2);
		body.addChild(rightThigh);
		body.addChild(leftThigh);
		body.addChild(neck1);
		body.addChild(tail);
		body.addChild(leftWing1);
		body.addChild(rightWing1);
		
		EnumMap<BonesBirdLegs, ModelRenderer> legMap = new EnumMap<BonesBirdLegs, ModelRenderer>(BonesBirdLegs.class);
		legMap.put(BonesBirdLegs.LEFT_KNEE, leftThigh);
		legMap.put(BonesBirdLegs.RIGHT_KNEE, rightThigh);
		legMap.put(BonesBirdLegs.LEFT_ANKLE, leftLeg);
		legMap.put(BonesBirdLegs.RIGHT_ANKLE, rightLeg);
		legMap.put(BonesBirdLegs.LEFT_METATARSOPHALANGEAL_ARTICULATIONS, leftAnkle);
		legMap.put(BonesBirdLegs.RIGHT_METATARSOPHALANGEAL_ARTICULATIONS, rightAnkle);
		legMap.put(BonesBirdLegs.LEFT_BACK_CLAW, leftToeB);
		legMap.put(BonesBirdLegs.RIGHT_BACK_CLAW, rightToeB);
		animationRun = new ModelAnimator<BonesBirdLegs>(legMap, AnimationRegistry.instance().getAnimation("bird_run"));
		
		EnumMap<BonesWings, ModelRenderer> wingMap = new EnumMap<BonesWings, ModelRenderer>(BonesWings.class);
		wingMap.put(BonesWings.LEFT_SHOULDER, leftWing1);
		wingMap.put(BonesWings.RIGHT_SHOULDER, rightWing1);
		wingMap.put(BonesWings.LEFT_ELBOW, leftWing2);
		wingMap.put(BonesWings.RIGHT_ELBOW, rightWing2);
		animationFlap = new ModelAnimator<BonesWings>(wingMap, AnimationRegistry.instance().getAnimation("wing_flap_2_piece"));
		
		setRotation(body, 0.7F, 0F, 0F);
		setRotation(rightThigh, -0.39F, 0F, 0.09F);
		setRotation(leftThigh, -0.39F, 0, -0.09F);
		setRotation(rightLeg, -0.72F, 0, 0);
		setRotation(leftLeg, -0.72F, 0, 0);
		setRotation(rightAnkle, 0.1F, 0.2F, 0);
		setRotation(leftAnkle, 0.1F, -0.2F, 0);
		setRotation(rightToeB, 1.34F, 0, 0);
		setRotation(rightToeR, -0.8F, -0.28F, -0.28F);
		setRotation(rightToeM, -0.8F, 0, 0);
		setRotation(rightToeL, -0.8F, 0.28F, 0.28F);
		setRotation(leftToeB, 1.34F, 0, 0);
		setRotation(leftToeR, -0.8F, 0.28F, 0.28F);
		setRotation(leftToeM, -0.8F, 0, 0);
		setRotation(leftToeL, -0.8F, -0.28F, -0.28F);
		setRotation(rightClawB, -36, 0, 0);
		
		setRotation(neck1, -0.18F, 0, 0);
		setRotation(neck2, 0.52F, 0, 0);
		setRotation(neck3, 0.26F, 0, 0);
		setRotation(head, -0.97F, 0, 0);
		setRotation(headFeathers, 0.38F, 0, 0);
		setRotation(backNeckFeathers, -1.11F, 0, 0);
		setRotation(leftNeckFeathers, -0.85F, -1.87F, 0.39F);
		setRotation(rightNeckFeathers, -0.85F, 1.87F, -0.39F);
		
		setRotation(tail, 0.3F, 0, 0);
	}

	public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
	{
		/*setRotation(rightClawB, 0.628F, 0, 0);
		setRotation(rightClawR, -0.52F, 0, 0);
		setRotation(rightClawM, -0.44F, 0, 0);
		setRotation(rightClawL, -0.52F, 0, 0);
		setRotation(rightClawB, 0.128F, 0, 0);
		setRotation(rightClawR, -0.12F, 0, 0);
		setRotation(rightClawM, -0.14F, 0, 0);
		setRotation(rightClawL, -0.12F, 0, 0);
		setRotation(leftClawB, 0.128F, 0, 0);
		setRotation(leftClawR, -0.62F, 0, 0);
		setRotation(leftClawM, -0.94F, 0, 0);
		setRotation(leftClawL, -0.72F, 0, 0);
		setRotation(lowerBeak, 0.3F, 0, 0);
		setRotation(upperBeak, -0.2F, 0, 0);*/
		body.render(par7);
	}
	
	public void setFlyingAnimations(AnimationState wingState, AnimationState legState, float roll, float headYaw, float headPitch, float parTick)
	{
		AnimationAction legAction = legState.getCurrentAction();
		AnimationAction wingAction = wingState.getCurrentAction();
		float flapProgress = wingState.getCurrentAnimationTimeInterp(parTick);
		float legProgress = legState.getCurrentAnimationTimeInterp(parTick);
		animationFlap.updateAnimation(flapProgress);
		animationRun.updateAnimation(legProgress);
		if(legAction == AnimationAction.RUN)
		{
			// 60=min, 93=max, 126=min, 159=max
			if(legProgress >= 38F / (331F + 17F) && legProgress < (170F + 17F) / (331F + 17F))
			{
				legProgress += 0.0; // Sync frame 38 to frame 60
				//if(legProgress >= 38F / 331F)
				//	legProgress -= (1.0 - 0.2235);
				
				float t = (float)Math.PI * 2 * 4 * legProgress / (1.0F - (38F / (170F + 17F))); // period = half of run cycle
				body.rotateAngleX += (float)(-Math.cos(t) * 0.1);
				neck1.rotateAngleX += (float)(Math.cos(t) * 0.08);
				body.rotationPointX += -(float)(Math.cos(t) * 1);
			}
		}
		
		if(wingAction == AnimationAction.WINGFLAP)
		{
			float flapCycle = flapProgress / (60F / 221F);
			// Set up/down reactionary body wobble for flapping motion
	        body.rotationPointY += MathHelper.cos(flapCycle * (float)Math.PI * 2F) * 1.4F;
	        rightThigh.rotateAngleX += MathHelper.cos(flapCycle * (float)Math.PI * 2F) * (5F / 180 * Math.PI);
	        leftThigh.rotateAngleX += MathHelper.cos(flapCycle * (float)Math.PI * 2F) * (5F / 180 * Math.PI);
	        tail.rotateAngleX += MathHelper.cos(flapCycle * (float)Math.PI * 2F) * (2F / 180 * Math.PI);
	       // head.rotateAngleX = (float) (-18F / 180 * Math.PI - MathHelper.cos(flapProgress * (float)Math.PI * 2F) * (2F / 180 * Math.PI));
		}
		body.rotateAngleZ = -roll / 180F * (float)Math.PI;
		
		// Calculate head+neck angles
		headPitch = (float)MathUtil.boundAngle180Deg(headPitch);
		if(headPitch > 37.16F)
			headPitch = 37.16F;
		else if(headPitch < -56.65F)
			headPitch = -56.65F;

		float pitchFactor = ((headPitch + 56.65F) / 93.8F);
		head.rotateAngleX += -0.96F + pitchFactor * (-1.1F - -0.96F);
		neck3.rotateAngleX += 0.378F + pitchFactor * (-0.15F - 0.378F);
		neck2.rotateAngleX += 0.4F + pitchFactor * (0 - 0.4F);
		neck1.rotateAngleX += 0.513F + pitchFactor * (-0.1F - 0.513F);
		
		headYaw = (float)MathUtil.boundAngle180Deg(headYaw);
		if(headYaw > 30.5F)
			headYaw = 30.5F;
		else if(headYaw < -30.5F)
			headYaw = -30.5F;
		
		float yawFactor = ((headYaw + 30.5F) / 61F);
		head.rotateAngleZ += 0.8F + yawFactor * 2 * -0.8F;
		neck3.rotateAngleZ += 0.38F + yawFactor * 2 * -0.38F;
		neck2.rotateAngleZ += 0.14F + yawFactor * 2 * -0.14F;
		head.rotateAngleY += -0.7F + yawFactor * 2 * 0.7F;
		neck3.rotateAngleY += -0.12F + yawFactor * 2 * 0.12F;
	}
	
	public void setRotationAngles(float limbPeriod, float limbMaxMovement, float ticksExisted, float headYaw, float entityPitch, float unitScale, Entity entity)
	{
		super.setRotationAngles(limbPeriod, limbMaxMovement, ticksExisted, headYaw, entityPitch, unitScale, entity);
        body.rotateAngleX += -0.7 + ((float)Math.PI / 2) - entityPitch / 180F * (float)Math.PI;
	}
	
	public void resetSkeleton()
	{
		setRotation(body, 0.7F, 0F, 0F);
		setRotation(rightThigh, -0.39F, 0F, 0.09F);
		setRotation(leftThigh, -0.39F, 0, -0.09F);
		setRotation(rightLeg, -0.72F, 0, 0);
		setRotation(leftLeg, -0.72F, 0, 0);
		setRotation(rightAnkle, 0.1F, 0.2F, 0);
		setRotation(leftAnkle, 0.1F, -0.2F, 0);
		setRotation(rightToeB, 1.34F, 0, 0);
		setRotation(rightToeR, -0.8F, -0.28F, -0.28F);
		setRotation(rightToeM, -0.8F, 0, 0);
		setRotation(rightToeL, -0.8F, 0.28F, 0.28F);
		setRotation(leftToeB, 1.34F, 0, 0);
		setRotation(leftToeR, -0.8F, 0.28F, 0.28F);
		setRotation(leftToeM, -0.8F, 0, 0);
		setRotation(leftToeL, -0.8F, -0.28F, -0.28F);
		//setRotation(rightClawB, -36, 0, 0);
		setRotation(neck1, 0, 0, 0);
		setRotation(neck2, 0, 0, 0);
		setRotation(neck3, 0, 0, 0);
		setRotation(head, 0, 0, 0);
		setRotation(headFeathers, 0.38F, 0, 0);
		setRotation(backNeckFeathers, -1.11F, 0, 0);
		setRotation(leftNeckFeathers, -0.85F, -1.87F, 0.39F);
		setRotation(rightNeckFeathers, -0.85F, 1.87F, -0.39F);
		setRotation(tail, 0.3F, 0, 0);
		
		body.setRotationPoint(0,  -27F, 0);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
