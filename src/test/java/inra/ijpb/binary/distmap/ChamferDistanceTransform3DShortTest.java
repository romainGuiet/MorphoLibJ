/**
 * 
 */
package inra.ijpb.binary.distmap;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class ChamferDistanceTransform3DShortTest
{

	/**
	 * Test method for {@link inra.ijpb.binary.distmap.ChamferDistanceTransform3DShort#distanceMap(ij.ImageStack)}.
	 */
	@Test
	public final void testDistanceMap()
	{
		// create 3D image containing a cube 
		ImageStack image = ImageStack.create(20, 20, 20, 8);
		for (int z = 2; z < 19; z++)
		{
			for (int y = 2; y < 19; y++)
			{
				for (int x = 2; x < 19; x++)
				{
					image.setVoxel(x, y, z, 255);
				}
			}
		}
		
		ChamferMask3D weights = ChamferMask3D.BORGEFORS;
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(weights, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
//		System.out.println("result:");
//		for (int x = 0; x < 100; x++)
//		{
//			System.out.print(((int)result.getVoxel(x, 10, 10)) + " ");
//		}
		double middle = result.getVoxel(10, 10, 10);
		assertEquals(9, middle, .1);
	}
	
	@Test
	public void testDistanceMap_FromCenter()
	{
		// create 3D image containing a cube 
		ImageStack image = ImageStack.create(11, 11, 11, 8);
		for (int z = 0; z < 11; z++)
		{
			for (int y = 0; y < 11; y++)
			{
				for (int x = 0; x < 11; x++)
				{
					image.setVoxel(x, y, z, 255);
				}
			}
		}
		image.setVoxel(5, 5, 5, 0);

		ChamferMask3D weights = ChamferMask3D.BORGEFORS;
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(weights, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
		assertEquals(1, result.getVoxel(4, 5, 5), 0.1);
		assertEquals(1, result.getVoxel(6, 5, 5), 0.1);
		assertEquals(1, result.getVoxel(4, 4, 5), 0.1);
		assertEquals(Math.round(5.0 / 3.0), result.getVoxel(4, 4, 4), 0.1);
		
		// Test some voxels at the cube corners
		int exp = (int) Math.round(5.0 * 5.0 / 3.0);
		assertEquals(exp, result.getVoxel( 0,  0,  0), .01);
		assertEquals(exp, result.getVoxel(10,  0,  0), .01);
		assertEquals(exp, result.getVoxel( 0, 10,  0), .01);
		assertEquals(exp, result.getVoxel(10, 10,  0), .01);
		assertEquals(exp, result.getVoxel( 0,  0, 10), .01);
		assertEquals(exp, result.getVoxel(10,  0, 10), .01);
		assertEquals(exp, result.getVoxel( 0, 10, 10), .01);
		assertEquals(exp, result.getVoxel(10, 10, 10), .01);
	}

}
