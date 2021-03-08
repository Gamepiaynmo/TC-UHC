package cn.topologycraft.uhc.gen;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import java.util.Arrays;

public class GenLayerPureLand extends GenLayer {

	public GenLayerPureLand(long p_i2125_1_) {
		super(p_i2125_1_);
	}

	@Override
	public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
		int[] aint = IntCache.getIntCache(areaWidth * areaHeight);
		Arrays.fill(aint, 1);
		return aint;
	}

}
