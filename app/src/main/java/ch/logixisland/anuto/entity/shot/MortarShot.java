package ch.logixisland.anuto.entity.shot;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.entity.Entity;
import ch.logixisland.anuto.entity.effect.Explosion;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.function.Function;
import ch.logixisland.anuto.util.math.function.SampledFunction;
import ch.logixisland.anuto.util.math.vector.Vector2;

public class MortarShot extends Shot {

    public final static float TIME_TO_TARGET = 1.5f;
    private final static float HEIGHT_SCALING_START = 0.5f;
    private final static float HEIGHT_SCALING_STOP = 1.0f;
    private final static float HEIGHT_SCALING_PEAK = 1.5f;

    private class StaticData {
        SpriteTemplate mSpriteTemplate;
    }

    private float mDamage;
    private float mRadius;
    private float mAngle;
    private SampledFunction mHeightScalingFunction;

    private StaticSprite mSprite;

    public MortarShot(Entity origin, Vector2 position, Vector2 target, float damage, float radius) {
        super(origin);
        setPosition(position);
        setSpeed(getDistanceTo(target) / TIME_TO_TARGET);
        setDirection(getDirectionTo(target));

        mDamage = damage;
        mRadius = radius;
        mAngle = RandomUtils.next(360f);

        StaticData s = (StaticData) getStaticData();

        float x1 = (float) Math.sqrt(HEIGHT_SCALING_PEAK - HEIGHT_SCALING_START);
        float x2 = (float) Math.sqrt(HEIGHT_SCALING_PEAK - HEIGHT_SCALING_STOP);
        mHeightScalingFunction = Function.quadratic()
                .multiply(-1f)
                .offset(HEIGHT_SCALING_PEAK)
                .shift(-x1)
                .stretch(GameEngine.TARGET_FRAME_RATE * TIME_TO_TARGET / (x1 + x2))
                .sample();

        mSprite = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate);
        mSprite.setListener(this);
        mSprite.setIndex(RandomUtils.next(4));
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.grenade, 4);
        s.mSpriteTemplate.setMatrix(0.7f, 0.7f, null, null);

        return s;
    }

    @Override
    public void init() {
        super.init();

        getGameEngine().add(mSprite);
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mSprite);
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        super.draw(sprite, canvas);

        float s = mHeightScalingFunction.getValue();
        canvas.scale(s, s);
        canvas.rotate(mAngle);
    }

    @Override
    public void tick() {
        super.tick();

        mHeightScalingFunction.step();
        if (mHeightScalingFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * TIME_TO_TARGET) {
            getGameEngine().add(new Explosion(getOrigin(), getPosition(), mDamage, mRadius));
            this.remove();
        }
    }
}
