# Threadpool

## Description
This project implements a customizable thread pool in Java, designed to optimize task execution by reusing a fixed number of threads instead of creating a thread for each new created task. It allows for dynamic adjustment of the thread count and provides task prioritization and management through a semaphore-based waitable queue. The API supports various task submission options and offers mechanisms to pause, resume, and shut down the pool, ensuring graceful handling of tasks.

## Features
- **Dynamic Thread Allocation**: Initialize with a default number of threads (equal to the number of available processor cores) or specify a custom count.
- **Task Submission with Priority**: Submit `Callable` or `Runnable` tasks with or without priority. Tasks are managed in a priority queue, with the next available thread picking up the highest-priority task.
- **Future Object**: Each submitted task returns a `Future` object, allowing for task status tracking.
- **Runtime Thread Adjustment**: Modify the number of active threads during execution. Adding threads increases the pool size, while reducing threads uses a poison pill technique to gracefully exit excess threads.
- **Pause and Resume**: Temporarily halt task execution with a 'sleeping pill' technique, resuming when ready.
- **Graceful Shutdown**: Overloaded shutdown methods provide options with or without a timeout, ensuring all tasks are completed before termination.

## Installation
Clone the repository and include the `.java` files in your Java project:
```bash
git clone https://github.com/amitaibar97/Threadpool.git
```

## Development Environment Setup

To use this Threadpool library effectively, ensure your development environment meets the following requirements:

### JDK Version
- This project requires Java Development Kit (JDK) version 8 or higher due to the use of lambda expressions and other Java 8 features. Ensure your JDK version meets this requirement.

### Integrated Development Environment (IDE)
- Any modern IDE that supports Java development (like IntelliJ IDEA, Eclipse, or VS Code with the Java extension) should be suitable.
- Configure your IDE to use the correct JDK version (8 or higher) in the project settings.

### Build Tools and Dependency Management
- If you're using build tools like Maven or Gradle, ensure your IDE supports these and is configured to use the project's build file (e.g., `pom.xml` for Maven projects).
- Ensure all project dependencies are correctly resolved through your IDE's dependency management system or build tool configuration.

## Usage

### Creating a ThreadPool

```// Default initialization using the number of available processor cores
ThreadPool threadPool = new ThreadPool();

// Custom initialization with a specific number of threads
ThreadPool threadPool = new ThreadPool(10);
```
### Submitting Tasks

```// Submit a Runnable task without priority
Future<?> future1 = threadPool.submit(new Runnable() {
    @Override
    public void run() {
        // Task implementation
    }
});

// Submit a Runnable task with priority
Future<?> future2 = threadPool.submit(new Runnable() {
    @Override
    public void run() {
        // Task implementation
    }
}, priority); // Replace 'priority' with an integer value representing task priority

// Submit a Callable task without priority
Future<String> future3 = threadPool.submit(new Callable<String>() {
    @Override
    public String call() throws Exception {
        // Task implementation
        return "Result";
    }
});

// Submit a Callable task with priority
Future<String> future4 = threadPool.submit(new Callable<String>() {
    @Override
    public String call() throws Exception {
        // Task implementation
        return "Result";
    }
}, priority); // Replace 'priority' with an integer value representing task priority
```

### Modifying Thread Count
```// Increase or decrease the number of threads in the pool
threadPool.setNumOfThreads(newCount); // Replace 'newCount' with the desired number of threads
```

### Pausing and Resuming Execution
```
// Pause task execution
threadPool.pause();

// Resume task execution
threadPool.resume();
```

### Shutdown
```// Graceful shutdown without timeout
threadPool.shutDown();

// Graceful shutdown with timeout
threadPool.shutDown(timeout); // Replace 'timeout' with the desired timeout value in milliseconds
```

### Awaiting Termination
```// Wait for all tasks to complete and the thread pool to shut down
threadPool.awaitTermination();
```

## Contributing
Contributions are welcome! Please open an issue to discuss proposed changes.

## License

This version of the README includes more detailed information about the API functionalities, including initialization, task submission, runtime adjustments, and shutdown procedures. Let me know if this meets your requirements or if there are any additional details you'd like to add.

