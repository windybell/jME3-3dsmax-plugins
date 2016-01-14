package com.jme3.asset.max3ds;

import static com.jme3.asset.max3ds.ChunkID.AMBIENT_COLOR;
import static com.jme3.asset.max3ds.ChunkID.BOUNDING_BOX;
import static com.jme3.asset.max3ds.ChunkID.CAMERA;
import static com.jme3.asset.max3ds.ChunkID.COLOR;
import static com.jme3.asset.max3ds.ChunkID.COORDINATE_AXES;
import static com.jme3.asset.max3ds.ChunkID.DIFFUSE_COLOR;
import static com.jme3.asset.max3ds.ChunkID.EDITOR;
import static com.jme3.asset.max3ds.ChunkID.FACES_DESCRIPTION;
import static com.jme3.asset.max3ds.ChunkID.FACES_MATERIAL;
import static com.jme3.asset.max3ds.ChunkID.FRAMES_CHUNK;
import static com.jme3.asset.max3ds.ChunkID.HIERARCHY_INFO;
import static com.jme3.asset.max3ds.ChunkID.KEYFRAMER;
import static com.jme3.asset.max3ds.ChunkID.LIGHT;
import static com.jme3.asset.max3ds.ChunkID.LIGHT_OFF;
import static com.jme3.asset.max3ds.ChunkID.MATERIAL;
import static com.jme3.asset.max3ds.ChunkID.MATERIAL_NAME;
import static com.jme3.asset.max3ds.ChunkID.MESH;
import static com.jme3.asset.max3ds.ChunkID.MESH_INFO;
import static com.jme3.asset.max3ds.ChunkID.MULTIPLIER;
import static com.jme3.asset.max3ds.ChunkID.NAMED_OBJECT;
import static com.jme3.asset.max3ds.ChunkID.NAME_AND_FLAGS;
import static com.jme3.asset.max3ds.ChunkID.OVERSHOOT;
import static com.jme3.asset.max3ds.ChunkID.PIVOT;
import static com.jme3.asset.max3ds.ChunkID.POSITION;
import static com.jme3.asset.max3ds.ChunkID.RANGE_END;
import static com.jme3.asset.max3ds.ChunkID.RANGE_START;
import static com.jme3.asset.max3ds.ChunkID.RAYTRACE;
import static com.jme3.asset.max3ds.ChunkID.RAY_TRACE_BIAS;
import static com.jme3.asset.max3ds.ChunkID.RECTANGULAR;
import static com.jme3.asset.max3ds.ChunkID.REFLECTION_BLUR;
import static com.jme3.asset.max3ds.ChunkID.ROTATION;
import static com.jme3.asset.max3ds.ChunkID.SCALE;
import static com.jme3.asset.max3ds.ChunkID.SCALE_TRACK;
import static com.jme3.asset.max3ds.ChunkID.SHADOWED;
import static com.jme3.asset.max3ds.ChunkID.SHADOW_MAP;
import static com.jme3.asset.max3ds.ChunkID.SHININESS;
import static com.jme3.asset.max3ds.ChunkID.SHININESS_STRENGTH;
import static com.jme3.asset.max3ds.ChunkID.SHOW_CONE;
import static com.jme3.asset.max3ds.ChunkID.SMOOTH;
import static com.jme3.asset.max3ds.ChunkID.SPECULAR_COLOR;
import static com.jme3.asset.max3ds.ChunkID.SPOTLIGHT;
import static com.jme3.asset.max3ds.ChunkID.SPOT_MAP;
import static com.jme3.asset.max3ds.ChunkID.SPOT_ROLL;
import static com.jme3.asset.max3ds.ChunkID.TEXTURE;
import static com.jme3.asset.max3ds.ChunkID.TEXTURE_COORDINATES;
import static com.jme3.asset.max3ds.ChunkID.TEXTURE_NAME;
import static com.jme3.asset.max3ds.ChunkID.TRANSPARENCY;
import static com.jme3.asset.max3ds.ChunkID.TRANSPARENCY_FALLOUT;
import static com.jme3.asset.max3ds.ChunkID.TWO_SIDED;
import static com.jme3.asset.max3ds.ChunkID.VERTEX_LIST;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.max3ds.anim.KeyFrameTrack;
import com.jme3.asset.max3ds.chunks.AxisChunk;
import com.jme3.asset.max3ds.chunks.BooleanChunk;
import com.jme3.asset.max3ds.chunks.BoundingBoxChunk;
import com.jme3.asset.max3ds.chunks.CameraChunk;
import com.jme3.asset.max3ds.chunks.Chunk;
import com.jme3.asset.max3ds.chunks.ColorChunk;
import com.jme3.asset.max3ds.chunks.FacesDescriptionChunk;
import com.jme3.asset.max3ds.chunks.FacesMaterialChunk;
import com.jme3.asset.max3ds.chunks.FloatChunk;
import com.jme3.asset.max3ds.chunks.FramesChunk;
import com.jme3.asset.max3ds.chunks.FramesDescriptionChunk;
import com.jme3.asset.max3ds.chunks.GlobalColorChunk;
import com.jme3.asset.max3ds.chunks.HierarchyInfoChunk;
import com.jme3.asset.max3ds.chunks.KeyFramerInfoChunk;
import com.jme3.asset.max3ds.chunks.LightChunk;
import com.jme3.asset.max3ds.chunks.MaterialChunk;
import com.jme3.asset.max3ds.chunks.MeshChunk;
import com.jme3.asset.max3ds.chunks.NamedObjectChunk;
import com.jme3.asset.max3ds.chunks.PercentageChunk;
import com.jme3.asset.max3ds.chunks.PivotChunk;
import com.jme3.asset.max3ds.chunks.PositionChunk;
import com.jme3.asset.max3ds.chunks.RotationChunk;
import com.jme3.asset.max3ds.chunks.ScaleChunk;
import com.jme3.asset.max3ds.chunks.SmoothingChunk;
import com.jme3.asset.max3ds.chunks.SpotLightChunk;
import com.jme3.asset.max3ds.chunks.StringChunk;
import com.jme3.asset.max3ds.chunks.TextureChunk;
import com.jme3.asset.max3ds.chunks.Vertex2ListChunk;
import com.jme3.asset.max3ds.chunks.Vertex3ListChunk;
import com.jme3.export.Savable;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.UserData;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Texture;

/**
 * A flyweight factory responsible for chopping the 
 * data up and sending it to the corresponding 
 * chunks(which are flyweights ala the flyweight pattern)
 * for processing.
 * This will sequentially read a 3ds file, load or
 * skip chunks and subchunks and initialize the data
 * for the chunks.  
 * <p>
 * Retrieved data may be stored as state in the ChunkChopper
 * via {@link #pushData} for use by other chunks.
 * <p> 
 * Features not supported; unknown chunks are skipped.
 */
public class ChunkChopper {
	
	private Logger logger = Logger.getLogger(ChunkChopper.class.getName());
	
	protected M3DLoader loader;
	
	private HashMap<Object, Object> dataMap;
	private HashMap<Object, Object> userData;
	
	protected ByteBuffer chunkBuffer;
	protected Integer chunkID;
	protected Chunk mainChunk;
	
	private Node scene;
	private Spatial currentObject;
	private String currentObjectName;
	private HashMap<String, Matrix4f> namedObjectCoordinateSystems = new HashMap<String, Matrix4f>();
	
	private int startFrame;
	private int endFrame;
	private List<KeyFrameTrack> meshTracks;
	private KeyFrameTrack currentTrack;
	
	/** This should be turned on by Loader3DS to view debugging information. */
	public static boolean debug;
	
    /**
     * Initialize the chunks
     */
    public ChunkChopper() {
    	
    	// Main Chunk
    	mainChunk = new Chunk("MainChunk");
    	
    	// SubChunks of MainChunk
    	Chunk editorChunk            = new Chunk("EditorChunk");
    	Chunk keyFramerChunk         = new Chunk("KeyFramerChunk");

    	// SubChunks Editor Chunk
        Chunk namedObjectChunk       = new NamedObjectChunk();
        Chunk materialChunk          = new MaterialChunk();
        
        // SubChunks of NamedObjectChunk
        // Chunk triangularMeshChunk    = new TriangularMeshChunk();
        
        // use this instead of TriangularMeshChunk for two reason:
        // 1) less vertex data
        // 2) better vertex normals
        Chunk triangularMeshChunk    = new MeshChunk();
        
        Chunk lightChunk             = new LightChunk();
        Chunk cameraChunk            = new CameraChunk();
        
    	// Global Chunk
    	Chunk floatChunk             = new FloatChunk();
    	Chunk stringChunk            = new StringChunk();
    	Chunk globalColorChunk       = new GlobalColorChunk();
    	Chunk booleanChunk           = new BooleanChunk();
    	Chunk percentageChunk        = new PercentageChunk();
    	Chunk colorChunk             = new ColorChunk();
    	
        Chunk facesDescriptionChunk  = new FacesDescriptionChunk();
        Chunk framesDescriptionChunk = new FramesDescriptionChunk();
        Chunk textureChunk           = new TextureChunk();
        
        Chunk keyFramerInfoChunk     = new KeyFramerInfoChunk();
        Chunk spotLightChunk         = new SpotLightChunk();
        Chunk framesChunk            = new FramesChunk();
        Chunk pivotChunk             = new PivotChunk();
        Chunk positionChunk          = new PositionChunk();
        Chunk rotationChunk          = new RotationChunk();
        Chunk scaleChunk             = new ScaleChunk();
        Chunk hierarchyInfoChunk     = new HierarchyInfoChunk();
        Chunk boundingBoxChunk       = new BoundingBoxChunk();
        Chunk vertex3ListChunk       = new Vertex3ListChunk();
        Chunk vertex2ListChunk       = new Vertex2ListChunk();
        Chunk axisChunk              = new AxisChunk();
        Chunk facesMaterialChunk     = new FacesMaterialChunk();
        Chunk smoothingChunk         = new SmoothingChunk();
        
        // Main Chunk
        mainChunk.addSubChunk(EDITOR, editorChunk);
        mainChunk.addSubChunk(KEYFRAMER, keyFramerChunk);
        
        // Editor Chunk
        editorChunk.addSubChunk(SCALE, floatChunk);
        editorChunk.addSubChunk(MATERIAL, materialChunk);
        editorChunk.addSubChunk(NAMED_OBJECT, namedObjectChunk);
        
        // Material Chunk
        materialChunk.addSubChunk(MATERIAL_NAME, stringChunk);
        materialChunk.addSubChunk(AMBIENT_COLOR, globalColorChunk);
        materialChunk.addSubChunk(DIFFUSE_COLOR, globalColorChunk);
        materialChunk.addSubChunk(SPECULAR_COLOR, globalColorChunk);
        materialChunk.addSubChunk(TEXTURE, textureChunk);
        materialChunk.addSubChunk(TWO_SIDED, booleanChunk);
        materialChunk.addSubChunk(SHININESS, percentageChunk);
        materialChunk.addSubChunk(SHININESS_STRENGTH, percentageChunk);
        materialChunk.addSubChunk(TRANSPARENCY, percentageChunk);
        materialChunk.addSubChunk(TRANSPARENCY_FALLOUT, percentageChunk);
        materialChunk.addSubChunk(REFLECTION_BLUR, percentageChunk);

        textureChunk.addSubChunk(TEXTURE_NAME, stringChunk);
        
        // NamedObjectChunk
        namedObjectChunk.addSubChunk(MESH, triangularMeshChunk);
        namedObjectChunk.addSubChunk(CAMERA, cameraChunk);// I think we don't need this camera
        namedObjectChunk.addSubChunk(LIGHT, lightChunk);// I think we don't need this light
        
        triangularMeshChunk.addSubChunk(VERTEX_LIST, vertex3ListChunk);
        triangularMeshChunk.addSubChunk(TEXTURE_COORDINATES, vertex2ListChunk);
        triangularMeshChunk.addSubChunk(FACES_DESCRIPTION, facesDescriptionChunk);
        triangularMeshChunk.addSubChunk(COORDINATE_AXES, axisChunk);

        facesDescriptionChunk.addSubChunk(FACES_MATERIAL, facesMaterialChunk);
        facesDescriptionChunk.addSubChunk(SMOOTH, smoothingChunk);
        
        lightChunk.addSubChunk(RANGE_START, floatChunk);
        lightChunk.addSubChunk(COLOR, colorChunk);
        lightChunk.addSubChunk(RANGE_END, floatChunk);
        lightChunk.addSubChunk(MULTIPLIER, floatChunk);
        lightChunk.addSubChunk(SPOTLIGHT, spotLightChunk);
        
        spotLightChunk.addSubChunk(LIGHT_OFF, booleanChunk);
        spotLightChunk.addSubChunk(RAYTRACE, booleanChunk);
        spotLightChunk.addSubChunk(SHADOWED, booleanChunk);
        spotLightChunk.addSubChunk(SHOW_CONE, booleanChunk);
        spotLightChunk.addSubChunk(RECTANGULAR, booleanChunk);
        spotLightChunk.addSubChunk(SHADOW_MAP, booleanChunk);
        spotLightChunk.addSubChunk(OVERSHOOT, booleanChunk);
        spotLightChunk.addSubChunk(SPOT_MAP, booleanChunk);
        spotLightChunk.addSubChunk(SPOT_ROLL, booleanChunk);
        spotLightChunk.addSubChunk(RAY_TRACE_BIAS, booleanChunk);
        
        keyFramerChunk.addSubChunk(FRAMES_CHUNK, framesChunk);
        keyFramerChunk.addSubChunk(MESH_INFO, keyFramerInfoChunk);
        // we don't need these thing in jME
        //keyFramerChunk.addSubChunk(AMBIENT_LIGHT_INFO, keyFramerInfoChunk);
        //keyFramerChunk.addSubChunk(CAMERA_INFO, keyFramerInfoChunk);
        //keyFramerChunk.addSubChunk(CAMERA_TARGET_INFO, keyFramerInfoChunk);
        //keyFramerChunk.addSubChunk(OMNI_LIGHT_INFO, keyFramerInfoChunk);
        //keyFramerChunk.addSubChunk(SPOT_LIGHT_TARGET_INFO, keyFramerInfoChunk);
        //keyFramerChunk.addSubChunk(SPOT_LIGHT_INFO, keyFramerInfoChunk);

        keyFramerInfoChunk.addSubChunk(NAME_AND_FLAGS, framesDescriptionChunk);
        keyFramerInfoChunk.addSubChunk(PIVOT, pivotChunk);
        keyFramerInfoChunk.addSubChunk(POSITION, positionChunk);
        keyFramerInfoChunk.addSubChunk(ROTATION, rotationChunk);
        keyFramerInfoChunk.addSubChunk(SCALE_TRACK, scaleChunk);
        keyFramerInfoChunk.addSubChunk(HIERARCHY_INFO, hierarchyInfoChunk);
        keyFramerInfoChunk.addSubChunk(BOUNDING_BOX, boundingBoxChunk);
    }

    /**
     * This sequentially parses the chunks out of the input stream and
     * constructs the 3D entities represented within.
     * A Chunk is a little endian data structure consists of a 
     * 6 byte header followed by subchunks and or data.  
     * The first short int(little endian) represent the id 
     * of the chunk.  The next int represent the total 
     * length of the chunk(total of data, subchunks and chunk header).
     * <p> 
     * The first chunk is the main chunk (id=4D4D) and its length
     * is always the length of the file. It only contains sub chunks.
     * Other chunks may contain data, subchunks or both.  If the format
     * of a chunk is unknown skipped.
     * <p>
     * Subclasses of chunk will all automagically load the subchunks.  
     * It is the programmers responsibility to ensure that the data 
     * preceeding the subchunks is loaded or skipped as 
     * required and that something useful is done with the data.  If data from the
     * subchunks is needed in order to initialize components then that code should be
     * placed in {@link Chunk#initialize}.  Otherwise the data may be dealt with in
     * {@link Chunk#loadData}.  Also, if a chunk has data preceeding its subchunks
     * it communicates how many bytes long that data is by returning it from loadData.
     * <p>
     * This chopper reads a file in order from beginning to end
     * @param inputStream the stream with the data to be parsed.
     * @param loader the loader that will be configured from the data.
     * @param modelName name of the model file for display purposes.
     * @param modelSize size in bytes of the file to read.
     * @throws IOException 
     */
    public synchronized Node loadSceneBase(InputStream inputStream, M3DLoader loader) throws IOException
    {
        this.loader = loader;
        this.scene = new Node();
        scene.setName("3DS@" + Integer.toHexString(scene.hashCode()));
        this.dataMap = new HashMap<Object, Object>();

        // FileChannel channel = null;
 		ReadableByteChannel channel = null;
 		channel = Channels.newChannel(inputStream);
 		chunkBuffer = getByteBuffer(channel);
 		

 		long begin = System.currentTimeMillis();
 		try {
 			loadSubChunks(mainChunk, 0);
 			createAnimation();
 		} catch (CannotChopException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Finished in " + (System.currentTimeMillis() - begin) + " ms");
 		logger.finest("FINISHED WITH THE SUBCHUNKS");
 		
 		// close channel
 		channel.close();
        return scene;
    }

	/**
     * Allocates and loads a byte buffer from the channel
     * @param channel the file channel to load the data from
     * @return a direct byte buffer containing all the data of the channel at position 0 
     */
    private ByteBuffer getByteBuffer(ReadableByteChannel channel) throws IOException
    {
    	// Read 3DS file header
 		ByteBuffer mainChunkBuffer = ByteBuffer.allocate(6);
 		mainChunkBuffer.order(ByteOrder.LITTLE_ENDIAN);
 		channel.read(mainChunkBuffer);
 		mainChunkBuffer.flip();

 		int mainChunkID = mainChunkBuffer.getShort();
 		int mainChunkLength = mainChunkBuffer.getInt();

 		// confirm that it's a 3ds file
 		assert mainChunkID == 0x4D4D;

 		// Allocates and loads a byte buffer from the channel
 		ByteBuffer chunkBuffer = ByteBuffer.allocate(mainChunkLength);
 		chunkBuffer.order(ByteOrder.LITTLE_ENDIAN);
 		chunkBuffer.put(mainChunkBuffer);
 		channel.read(chunkBuffer);

 		// Ready
 		chunkBuffer.flip();
 		
 		logger.finest("\n\n\n STARTING SUBCUNKS " + (mainChunkLength - 61));
        return chunkBuffer;
    }

	/**
	 * The base class Chunk takes care of loading subchunks for all chunks
	 * types. It occurs as follows:
	 * <ol>
	 * <li>The chunk id (short) is read
	 * <li>The chunk length(int) is read
	 * <li>A subchunk is looked up from the map of publish subchunk types of the
	 * current chunk.
	 * <li>If it isn't found during the lookup it is skipped.
	 * <li>Otherwise it is requested to {@link #pushData}
	 * <li>The return value, if there is one, is used to determine where its
	 * next subchunk is. A return value of 0 signifies that the next subchunk is
	 * nigh.
	 * <li>The chunk's subchunks are then loaded.
	 * <li>The chunks initialize method is called.
	 * </ol>
	 */
	private void loadSubChunks(Chunk parentChunk, int level)
			throws CannotChopException {
		level++;
		while (chunkBuffer.hasRemaining())// hasRemaining() evaluates limit - position.
		{
			chunkID = new Integer(chunkBuffer.getShort());
			Chunk chunk = parentChunk.getSubChunk(chunkID);

			int currentChunkLength = chunkBuffer.getInt() - 6; // length includes this 6 byte header.
			int finishedPosition = chunkBuffer.position() + currentChunkLength;
			int previousLimit = chunkBuffer.limit();
			
			chunkBuffer.limit(chunkBuffer.position() + currentChunkLength);

			if (debug) {
				Debug.debug(parentChunk, level, chunkID, currentChunkLength, chunkBuffer.position(), chunkBuffer.limit());
			}
			if (chunk != null && currentChunkLength != 0) {
				try {
					chunk.loadData(this);
				} catch (BufferUnderflowException e) {
					chunkBuffer.position(finishedPosition);
					chunkBuffer.limit(previousLimit);
					throw new CannotChopException(" tried to read too much data from the buffer. Trying to recover.", e);
				}
				try {
					if (chunkBuffer.hasRemaining()) {
						loadSubChunks(chunk, level);
					}
					chunk.initialize(this);
				} catch (CannotChopException e) {
					logger.log(Level.SEVERE, chunk.toString() + "Trying to continue");
				}
			}

			chunkBuffer.position(finishedPosition);
			chunkBuffer.limit(previousLimit);
		}
	}
	
    /**
     * Called to set the coordinate system transform for an object named
     * objectName. 
     * This is the first transform.
     */
    public void setCoordinateSystem(String objectName, Matrix4f coordinateSystem)
    {
        namedObjectCoordinateSystems.put(objectName, coordinateSystem);
    }
    
    public void setStartFrame(int startFrame) {
    	this.startFrame = startFrame;
    }
    public void setEndFrame(int endFrame) {
    	this.endFrame = endFrame;
    }
    /**
     * Add an objectTrack to scene.
     * @param track
     */
	public void addObjectTrack(KeyFrameTrack track) {
		if (meshTracks == null) {
			meshTracks = new ArrayList<KeyFrameTrack>();
		}
		meshTracks.add(track);
		currentTrack = track;
	}
	/**
	 * Gets the key framer track These should be their own objects instead of
	 * chunks.
	 */
	public KeyFrameTrack getCurrentTrack() {
		return currentTrack;
	}
	
	/**
	 * Adds a group to the choppers scene group and sets the current name and
	 * group.
	 * 
	 * @param the
	 *            name of the object to add which will also be the current name
	 *            of the object the chopper is working with.
	 * @param group
	 *            the current group that the chopper will be adding things too.
	 */
	public void attachChild(Spatial object) {
		scene.attachChild(object);
		currentObject = object;
		currentObjectName = object.getName();
	}
	
	public void detachChild(Spatial object) {
		if (scene.hasChild(object)) {
			scene.detachChild(object);
		}
	}

	/**
	 * Gets the name of the current object the chopper is working with. The
	 * value returned from this generally is either set by a NamedObjectChunk
	 * and is the name of the name of the last object added or is the name set
	 * by setObjectName.
	 * 
	 * @return the name of the current object being constructed.
	 */
	public String getObjectName() {
		return currentObjectName;
	}

	/**
	 * Sets the name of the current object. The name of the current object can
	 * also be set with {@link #addObject}
	 * 
	 * @param name
	 *            the name that the current object should be set to.
	 */
	public void setObjectName(String name) {
		currentObjectName = name;
	}

	/**
	 * Gets the group for the current object the chopper is working with. The
	 * value returned from this generally gets set by a NamedObjectChunk and is
	 * the name of the last object added.
	 * 
	 * @return the group for the current object being constructed.
	 */
	public Spatial getCurrentObject() {
		return currentObject;
	}


	/**
	 * Sets a named object in the loader.
	 * 
	 * @param key
	 *            the key name of the object
	 * @param value
	 *            the named Object.
	 */
	public void setNamedObject(Object key, Object data) {
		if (userData == null) {
            userData = new HashMap<Object, Object>();
        }
		
        if(data == null){
            userData.remove(key);            
        }else if (data instanceof Savable) {
            userData.put(key, (Savable) data);
        } else {
            userData.put(key, new UserData(UserData.getObjectType(data), data));
        }
	}
	
	/**
	 * Sets a named Object in the loader.
	 * 
	 * @param key
	 *            the key used as the name for which the object will be returned
	 */
	public Object getNamedObject(Object key) {
		if (key == null) {
			return null;
		}
		
		if (userData == null) {
            return null;
        }

        Object s = userData.get(key);
        if (s instanceof UserData) {
            return ((UserData) s).getValue();
        } else {
            return s;
        }
	}
	
	/**
	 * Returns true if there have been lights loaded.
	 * 
	 * @return true if there are lights.
	 */
	public boolean hasLights() {
		return scene.getLocalLightList().size() > 0;
	}
	
	/**
	 * Adds a light to the scene.
	 * 
	 * @param light
	 *            the light to add to the scene.
	 */
	public void addLight(Light light) {
		scene.addLight(light);
	}
	
	/**
	 * Gets and cast the named object for the key provided. Its an error if its
	 * not a transform group.
	 */
	public Spatial getChild(String key) {
		return scene.getChild(key);
	}
	
	/**
	 * Used to store data that may later be used by another chunk.
	 * 
	 * @param key
	 *            the look up key.
	 * @param data
	 *            the data to store.
	 */
	public void pushData(Object key, Object data) {
		dataMap.put(key, data);
	}

	/**
	 * Gets a datum that had been retrieved and stored via {@link #pushData}
	 * earlier and removes it.
	 * 
	 * @param key
	 *            the key used to store the datum earlier.
	 */
	public Object popData(Object key) {
		Object retVal = dataMap.remove(key);
		return retVal;
	}
	
	/**
	 * Gets a long from the chunk Buffer
	 */
	public long getLong() {
		return chunkBuffer.getLong();
	}

	/**
	 * Reads a short and returns it as a signed int.
	 */
	public int getShort() {
		return chunkBuffer.getShort();
	}

	/**
	 * Reads a short and returns it as an unsigned int.
	 */
	public int getUnsignedShort() {
		return chunkBuffer.getShort() & 0xFFFF;
	}

	/**
	 * reads a float from the chunkBuffer.
	 */
	public float getFloat() {
		return chunkBuffer.getFloat();
	}

	/**
	 * Reads 3 floats x,z,y from the chunkbuffer. Since 3ds has z as up and y as
	 * pointing in whereas java3d has z as pointing forward and y as pointing
	 * up; this returns new Point3f(x,-z,y)
	 */
	public Vector3f getVector3f() {
		float x = chunkBuffer.getFloat();
		float z = -chunkBuffer.getFloat();
		float y = chunkBuffer.getFloat();
		return new Vector3f(x, y, z);
	}

	/**
	 * Reads an int and returns it
	 * 
	 * @return the int read
	 */
	public int getInt() {
		return chunkBuffer.getInt();
	}

	/**
	 * Reads an int and returns it unsigned, any ints greater than MAX_INT will
	 * break.
	 */
	public int getUnsignedInt() {
		return chunkBuffer.getInt() & 0xFFFFFFFF;
	}

	/**
	 * Reads a byte, unsigns it, returns the corresponding int.
	 * 
	 * @return the unsigned int corresponding to the read byte.
	 */
	public int getUnsignedByte() {
		return chunkBuffer.get() & 0xFF;
	}

	/**
	 * Reads a number of bytes corresponding to the number of bytes left in the
	 * current chunk and returns an array containing them.
	 * 
	 * @return an array containing all the bytes for the current chunk.
	 */
	public byte[] getChunkBytes() {
		byte[] retVal = new byte[chunkBuffer.limit() - chunkBuffer.position()];
		get(retVal);
		return retVal;
	}

	/**
	 * Fills bytes with data from the chunk buffer.
	 * 
	 * @param bytes
	 *            the array to fill with data.
	 */
	public void get(byte[] bytes) {
		chunkBuffer.get(bytes);
	}

	/**
	 * Sets the data map used to store values that chunks may need to retrieve
	 * later.
	 * 
	 * @param dataMap
	 *            the hashmap that will be used to store and retrieve values for
	 *            use by chunks.
	 */
	public void setDataMap(HashMap<Object, Object> dataMap) {
		this.dataMap = dataMap;
	}

	/**
	 * This reads bytes until it gets 0x00 and returns the corresponding string.
	 */
	public String getString() {
		StringBuffer stringBuffer = new StringBuffer();
		char charIn = (char) chunkBuffer.get();
		while (charIn != 0x00) {
			stringBuffer.append(charIn);
			charIn = (char) chunkBuffer.get();
		}
		return stringBuffer.toString();
	}

	/**
	 * Gets the id of the current chunk.
	 * 
	 * @return id of the current chunk as read from the chunkBuffer. It will be
	 *         a signed <code>short</code>.
	 */
	public Integer getID() {
		return chunkID;
	}

	public Material getDefaultMaterial() {
		return loader.getDefaultMaterial();
	}

	public Material getLightMaterial() {
		return loader.getLightMaterial();
	}
	
	public Texture createTexture(String textureName) {
		return loader.createTexture(textureName);
	}
	
    /**
     * Retrieves the named object for the each key framer
     * inserts the rotation, position and pivot transformations for frame 0
     * and assigns the coordinate system to it.
     *
     * The inverse of the local coordinate system converts from 3ds 
     * semi-absolute coordinates (what is in the file) to local coordinates.
     *
     * Then these local coordinates are converted with matrix 
     * that will instantiate them to absolute coordinates:
     * Xabs = sx a1 (Xl-Px) + sy a2 (Yl-Py) + sz a3 (Zl-Pz) + Tx
     * Yabs = sx b1 (Xl-Px) + sy b2 (Yl-Py) + sz b3 (Zl-Pz) + Ty
     * Zabs = sx c1 (Xl-Px) + sy c2 (Yl-Py) + sz c3 (Zl-Pz) + Tz
     * Where:
     * (Xabs,Yabs,Zabs) = absolute coordinate
     * (Px,Py,Pz) = mesh pivot (constant)
     * (X1,Y1,Z1) = local coordinates
     *
     */
	private void reCalculateVertex(Skeleton ske) {
		
		for(KeyFrameTrack track : meshTracks) {
			String name = track.name;
			Spatial child = scene.getChild(name);
			// Bone bone = ske.getBone(name);
			
			Matrix4f coordinateSystem = namedObjectCoordinateSystems.get(name);
			if (coordinateSystem == null) coordinateSystem = new Matrix4f();
			Vector3f pivot = findPivot(name);
			System.out.println(name + " coordinate system:\n" + coordinateSystem + "\nPivot:" + pivot);

			// reset mesh vertex
			if (child != null && child instanceof Geometry) {
				Geometry geom = (Geometry)child;
				reCalculate(geom, coordinateSystem, pivot);
			}
			
		}
	}
	


	/**
	 * Find the Geometry's pivot by it's name
	 * @param name
	 * @return
	 */
	private Vector3f findPivot(String name) {
		Vector3f rVal = null;
		
		if (meshTracks == null) return new Vector3f();
		
		for(KeyFrameTrack track : meshTracks) {
			if (track.name.equals(name)) {
				rVal = track.pivot;
				break;
			}
		}
		
		if (rVal == null) rVal = new Vector3f();
		
		return rVal;
	}
	
	
	private void reCalculate(Geometry geom, Matrix4f coordinateSystem, Vector3f pivot) {
		Vector3f tmp = new Vector3f();
		Matrix4f coordinateTransform = new Matrix4f(coordinateSystem).invertLocal();
		
		// get vertex data
		Mesh mesh = geom.getMesh();
		FloatBuffer fb = (FloatBuffer)mesh.getBuffer(Type.Position).getData();
		fb.flip();// ready to read
		
		// recalculate vertex
		int limit = fb.limit();
		for(int i=0; i<limit; i+=3) {
			tmp.x = fb.get();
			tmp.y = fb.get();
			tmp.z = fb.get();
			
			tmp = coordinateTransform.mult(tmp);
			tmp.subtractLocal(pivot);
			
			fb.put(i, tmp.x);
			fb.put(i+1, tmp.y);
			fb.put(i+2, tmp.z);
		}
	}
	
	/**
	 * Create Animation !
	 * 
	 */
    private void createAnimation() {
    	System.out.printf("start: %d stop:%d\n", startFrame, endFrame);
    	
    	if (endFrame == 0 || meshTracks == null) return;
    	
    	
    	/** add controls to the model*/
		AnimControl ac = createAnimControl();
		scene.addControl(ac);
		
		Skeleton ske = ac.getSkeleton();
		SkeletonControl sc = new SkeletonControl(ske);
		scene.addControl(sc);
		
		/** recalcualte the vertex coordinate */
		reCalculateVertex(ske);

		/** skinning the model */
		for(Spatial child : scene.getChildren()) {
			if (child instanceof Geometry) {
				Geometry geom = (Geometry)child;
				String name = geom.getName();
				skinning(geom.getMesh(), (byte)ske.getBoneIndex(name));
			}
		}
	}
    
	/**
	 * Create an AnimControl, it contains an Animation with 3 BoneTracks.
	 * @return
	 */
	private AnimControl createAnimControl() {
		
		Skeleton ske = buildSkeleton();
		
		AnimControl animControl = new AnimControl(ske);
		
		Animation anim = buildAnimation(ske);
		
		animControl.addAnim(anim);
		
		return animControl;
	}

	/**
	 * Create a Skeleton with data of KeyFrameTracks.
	 * 
	 * @return
	 */
    private Skeleton buildSkeleton() {
    	int boneSize = meshTracks.size();
    	Bone[] bones = new Bone[boneSize];
    	
    	for(int i=0; i<meshTracks.size(); i++) {
    		KeyFrameTrack track = meshTracks.get(i);
    		
    		bones[track.ID] = new Bone(track.name);
    		if (track.fatherID != -1)
    			bones[track.fatherID].addChild(bones[track.ID]);
    		
    		Vector3f initTranslation = new Vector3f();
    		Quaternion initRotation = new Quaternion();
    		Vector3f initScale = new Vector3f();
    		
    		if (track.locateTrack(0) != null) {
    			initTranslation.set(track.locateTrack(0).position);
    			initRotation.set(track.locateTrack(0).rotation);
    			initScale.set(track.locateTrack(0).scale);
    		}
    		bones[track.ID].setBindTransforms(initTranslation, initRotation, initScale);;
    		
    	}
    	
    	Skeleton skeleton = new Skeleton(bones);
    	
    	System.out.println(skeleton);
    	return skeleton;
    }
    
    /**
     * Create animation
     * @param ske
     * @return
     */
    private Animation buildAnimation(Skeleton ske) {
    	// Calculate animation length
    	float speed = 30f;
    	float length = endFrame / speed;
    	
    	Animation anim = new Animation("3DS Animation", length);
    	
    	for(KeyFrameTrack track : meshTracks) {
    		int targetBoneIndex = ske.getBoneIndex(track.name);
    		
    		anim.addTrack(track.toBoneTrack(targetBoneIndex, speed));
    	}
    	return anim;
    }
    
	/**
	 * Skinning the mesh
	 * @param mesh
	 * @param targetBoneIndex
	 */
	private void skinning(Mesh mesh, byte targetBoneIndex) {
		if (targetBoneIndex == -1) return;
		
		// Calculate vertex count
		int limit = mesh.getBuffer(Type.Position).getData().limit();
		// Notice: i should call mesh.getMode() to decide how many 
		// floats is used for each vertex. Default mode is Mode.Triangles
		int vertexCount = limit/3;// by default
		
		int boneIndexCount = vertexCount * 4;
		byte[] boneIndex = new byte[boneIndexCount];
		float[] boneWeight = new float[boneIndexCount];

		// calculate bone indices and bone weights;
		for(int i=0; i<boneIndexCount; i+=4) {
			boneIndex[i] = targetBoneIndex;
			// I don't need the other 3 indices so I discard them
			boneIndex[i+1] = 0;
			boneIndex[i+2] = 0;
			boneIndex[i+3] = 0;
			
			boneWeight[i] = 1;
			// I don't need the other 3 indices so I discard them
			boneWeight[i+1] = 0;
			boneWeight[i+2] = 0;
			boneWeight[i+3] = 0;
		}
		mesh.setMaxNumWeights(4);
		
		// apply software skinning
		mesh.setBuffer(Type.BoneIndex, 4, boneIndex);
		mesh.setBuffer(Type.BoneWeight, 4, boneWeight);

		mesh.generateBindPose(true);
	}
}
