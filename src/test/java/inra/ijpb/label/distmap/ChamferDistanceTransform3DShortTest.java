/**
 * 
 */
package inra.ijpb.label.distmap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.ImageStack;
import inra.ijpb.binary.distmap.ChamferMask3D;

/**
 * @author dlegland
 *
 */
public class ChamferDistanceTransform3DShortTest
{

	/**
	 * Test method for {@link inra.ijpb.label.distmap.ChamferDistanceTransform3DShort#distanceMap(ij.ImageStack)}.
	 */
	@Test
	public void testDistanceMap()
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
		
		ChamferMask3D mask = ChamferMask3D.BORGEFORS;
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(mask, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
//		System.out.println("result:");
//		for (int x = 0; x < 100; x++)
//		{
//			System.out.print(((int)result.getVoxel(x, 50, 50)) + " ");
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

		ChamferMask3D mask = ChamferMask3D.BORGEFORS;
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(mask, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
		assertEquals(1, result.getVoxel(4, 5, 5), 0.1);
		assertEquals(1, result.getVoxel(6, 5, 5), 0.1);
		assertEquals(1, result.getVoxel(4, 4, 5), 0.1);
		assertEquals(Math.round(5.0 / 3.0), result.getVoxel(4, 4, 4), .1);
		
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

	/**
	 * Test propagation of distance maps within touching labels
	 */
	@Test
	public void testDistanceMap_Labels()
	{
		// create 3D image containing eight cubes with labels between 1 and 8 
		ImageStack image = ImageStack.create(11, 11, 11, 8);
		for (int z = 0; z < 3; z++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int x = 0; x < 3; x++)
				{
					image.setVoxel(x+1, y+1, z+1, 1);
					image.setVoxel(x+4, y+1, z+1, 2);
					image.setVoxel(x+1, y+4, z+1, 3);
					image.setVoxel(x+4, y+4, z+1, 4);
					image.setVoxel(x+1, y+1, z+4, 5);
					image.setVoxel(x+4, y+1, z+4, 6);
					image.setVoxel(x+1, y+4, z+4, 7);
					image.setVoxel(x+4, y+4, z+4, 8);
				}
			}
		}

		ChamferMask3D mask = ChamferMask3D.BORGEFORS;
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(mask, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
		assertEquals(2, result.getVoxel(2, 2, 2), .1);
		assertEquals(2, result.getVoxel(5, 2, 2), .1);
		assertEquals(2, result.getVoxel(2, 5, 2), .1);
		assertEquals(2, result.getVoxel(5, 5, 2), .1);
		assertEquals(2, result.getVoxel(2, 2, 5), .1);
		assertEquals(2, result.getVoxel(5, 2, 5), .1);
		assertEquals(2, result.getVoxel(2, 5, 5), .1);
		assertEquals(2, result.getVoxel(5, 5, 5), .1);
		
		assertEquals(1, result.getVoxel(3, 3, 3), .1);
		assertEquals(1, result.getVoxel(4, 3, 3), .1);
		assertEquals(1, result.getVoxel(3, 4, 3), .1);
		assertEquals(1, result.getVoxel(4, 4, 3), .1);
		assertEquals(1, result.getVoxel(3, 3, 4), .1);
		assertEquals(1, result.getVoxel(4, 3, 4), .1);
		assertEquals(1, result.getVoxel(3, 4, 4), .1);
		assertEquals(1, result.getVoxel(4, 4, 4), .1);
	}

}
