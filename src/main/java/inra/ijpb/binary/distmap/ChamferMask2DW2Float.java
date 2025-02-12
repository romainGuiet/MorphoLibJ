/**
 * 
 */
package inra.ijpb.binary.distmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of Chamfer Weights that manages two types of offsets,
 * corresponding to orthogonal, and diagonal neighbors.
 * 
 * This implementation manages two series of weights, one for integer
 * computation, the other one for floating-point computation.
 * 
 * @see ChamferMask3DW3
 * @see ChamferMask3DW4
 * 
 * @author dlegland
 */
public class ChamferMask2DW2Float extends ChamferMask2D
{
	short[] shortWeights;
	float[] floatWeights;

	public ChamferMask2DW2Float(short[] shortWeights, float[] floatWeights)
	{
		if (shortWeights.length != 2)
		{
			throw new RuntimeException("Number of short weights must be 2, not " + shortWeights.length);
		}
		if (floatWeights.length != 2)
		{
			throw new RuntimeException("Number of float weights must be 2, not " + floatWeights.length);
		}

		this.shortWeights = shortWeights;
		this.floatWeights = floatWeights;
	}

	@Override
	public Collection<ShortOffset> getForwardOffsets()
	{
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
		offsets.add(new ShortOffset(-1, -1, shortWeights[1]));
		offsets.add(new ShortOffset( 0, -1, shortWeights[0]));
		offsets.add(new ShortOffset(+1, -1, shortWeights[1]));
		offsets.add(new ShortOffset(-1,  0, shortWeights[0]));
	
		return offsets;
	}

	@Override
	public Collection<ShortOffset> getBackwardOffsets()
	{
		// create array of backward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, +1, shortWeights[1]));
		offsets.add(new ShortOffset( 0, +1, shortWeights[0]));
		offsets.add(new ShortOffset(+1, +1, shortWeights[1]));
		offsets.add(new ShortOffset(+1,  0, shortWeights[0]));

		return offsets;
	}

	@Override
	public Collection<FloatOffset> getForwardFloatOffsets()
	{
		// create array of forward shifts
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();
	
		// offsets in the current plane
		offsets.add(new FloatOffset(-1, -1, floatWeights[1]));
		offsets.add(new FloatOffset( 0, -1, floatWeights[0]));
		offsets.add(new FloatOffset(+1, -1, floatWeights[1]));
		offsets.add(new FloatOffset(-1,  0, floatWeights[0]));
	
		return offsets;
	}

	@Override
	public Collection<FloatOffset> getBackwardFloatOffsets()
	{
		// create array of backward shifts
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();

		// offsets in the current plane
		offsets.add(new FloatOffset(-1, +1, floatWeights[1]));
		offsets.add(new FloatOffset( 0, +1, floatWeights[0]));
		offsets.add(new FloatOffset(+1, +1, floatWeights[1]));
		offsets.add(new FloatOffset(+1,  0, floatWeights[0]));

		return offsets;
	}

	@Override
	public double getNormalizationWeight()
	{
		return floatWeights[0];
	}

	@Override
	public short getShortNormalizationWeight()
	{
		return shortWeights[0];
	}
}
