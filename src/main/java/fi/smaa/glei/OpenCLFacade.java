package fi.smaa.glei;

import static org.jocl.CL.*;
import org.jocl.CL;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

public class OpenCLFacade {

	private cl_context context;
	private cl_command_queue commandQueue;
	
	private int platformIndex = 0;
	private int deviceIndex = 0;
	private cl_device_id devices[];
	private cl_context_properties contextProperties;

	public OpenCLFacade() {
		initEngine();
		initDevice(deviceIndex);
	}
	
	public int getNrDevices() {
		return devices.length;
	}

	public void initEngine() {
		final long deviceType = CL_DEVICE_TYPE_ALL;

		// Enable exceptions and subsequently omit error checks in this sample
		CL.setExceptionsEnabled(true);

		// Obtain the number of platforms
		int numPlatformsArray[] = new int[1];
		clGetPlatformIDs(0, null, numPlatformsArray);
		int numPlatforms = numPlatformsArray[0];

		// Obtain a platform ID
		cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
		clGetPlatformIDs(platforms.length, platforms, null);
		cl_platform_id platform = platforms[platformIndex];

		contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

		// Obtain the number of devices for the platform
		int numDevicesArray[] = new int[1];
		clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
		int numDevices = numDevicesArray[0];

		devices = new cl_device_id[numDevices];
		clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
	}

	private void initDevice(int index) {
		cl_device_id device = devices[index];

		// Create a context for the selected device
		context = clCreateContext(
				contextProperties, 1, new cl_device_id[]{device}, 
				null, null, null);

		// Create a command-queue
		commandQueue = clCreateCommandQueue(context, devices[0], 0, null);
	}
}
