package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;

import java.io.File;
import java.io.IOException;

public class TaskPregenerate extends Task {
	private int size;
	private int x;
	private int z;

	private boolean inited;
	private WorldServer world;
	private ChunkProviderServer provider;
	static int lastRatio = 0;

	public TaskPregenerate(int borderSize, WorldServer worldServer) {
		size = borderSize;
		world = worldServer;
		inited = false;
		x = z = -size;
		provider = world.getChunkProvider();
	}

	private void genOneChunk() {
		if (this.hasFinished())
			return;

		if (!inited) {
			if (x > size) {
				if (z > size) {
					inited = true;
					x = z = -size + 1;
					return;
				}
				provider.provideChunk(-size, z);
				z++;
				return;
			}
			provider.provideChunk(x, -size);
			x++;
			return;
		}

		if (provider.getLoadedChunk(x - 1, z - 1) == null)
			provider.loadChunk(x - 1, z - 1);
		if (provider.getLoadedChunk(x, z - 1) == null)
			provider.loadChunk(x, z - 1);
		if (provider.getLoadedChunk(x - 1, z) == null)
			provider.loadChunk(x - 1, z);
		provider.provideChunk(x, z);

		x++;
		if (x > size) {
			x = -size + 1;
			z++;
			float ratio = (float) (z + size) * 100 / (2 * size + 1);
			GameManager.LOG.info(String.format("%.2f%% chunks of %s loaded.", (float) (z + size) * 100 / (2 * size + 1), world.provider.getDimensionType().getName()));
			if (ratio + 1e-4 > lastRatio) {
				GameManager.instance.broadcastMessage(String.format("Chunk generate progress: %d%%", lastRatio));
				lastRatio += 10;
			}
		}
	}

	@Override
	public void onUpdate() {
		for (int i = 0; i < 4; i++) {
			genOneChunk();
		}
	}

	@Override
	public boolean hasFinished() {
		return inited && z > size;
	}

	@Override
	public void onFinish() {
		try {
			File preload = new File(GameManager.instance.getMinecraftServer().getFolderName() + "/preload");
			if (!preload.exists())
				preload.createNewFile();
			GameManager.instance.setPregenerateComplete();
		} catch (IOException e) {

		}
	}
}
