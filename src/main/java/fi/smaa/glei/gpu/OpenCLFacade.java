package fi.smaa.glei.gpu;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;

import static org.jocl.CL.CL_DEVICE_TYPE_GPU;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clGetDeviceInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;


public class OpenCLFacade {

	private cl_context context;
	private cl_command_queue commandQueue;
	
	private int platformIndex = 0;
	private int deviceIndex = 0;
	private cl_device_id devices[];
	private cl_context_properties contextProperties;
	private int maxWorkGroupSize;

	public OpenCLFacade() {
		initEngine();
		initDevice(deviceIndex);
	}
	
	public String loadProgram(String fileName) throws IOException {
		InputStream istream = getClass().getClassLoader().getResourceAsStream(fileName);
		BufferedReader rdr = new BufferedReader(new InputStreamReader(istream));
		String res = "";
		String curLine = null;
		do {
			curLine = rdr.readLine();
			if (curLine != null) {
				res += curLine;
			}
		} while (curLine != null);
		return res;
	}
	
	public cl_program buildProgram(String fName) throws IOException {
		cl_program program = clCreateProgramWithSource(context, 1, new String[]{ loadProgram(fName) }, null, null);
        clBuildProgram(program, 0, null, null, null, null);
		return program;
	}
	
	public int getNrDevices() {
		return devices.length;
	}

	public void initEngine() {
		final long deviceType = CL_DEVICE_TYPE_GPU;

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
		
		// store max work group size
		int[] wsBuf = new int[1];
		long[] ret = new long[1];
		clGetDeviceInfo(device, CL.CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.cl_int, Pointer.to(wsBuf), ret);
		maxWorkGroupSize = wsBuf[0];
	}

	public cl_context getContext() {
		return context;
	}
	
	public int getMaxWorkGroupSize() {
		return maxWorkGroupSize;
	}
	
	public cl_mem createIntArgBuffer(int value) {
		int[] argv = new int[1];
		argv[0] = value;
		return clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_int, Pointer.to(argv), null);
	}

	public cl_command_queue getCommandQueue() {
		return commandQueue;
	}
	
	@Override
	public void finalize() {
		clReleaseCommandQueue(commandQueue);
		clReleaseContext(context);
	}
}
