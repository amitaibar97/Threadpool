# Threadpool

## Description
This project implements a thread pool in Java. A thread pool reuses a fixed number of threads to execute tasks, which can improve performance and system resource utilization. The `ThreadPool` class manages the pool, and the `SemWaitableQueue` class handles task queuing with semaphore-based synchronization.

## Installation
To use the thread pool in your project, clone this repository and include the `.java` files in your Java project.

```bash
git clone https://github.com/amitaibar97/Threadpool.git

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

## Usage

### Creating a ThreadPool

```java
// Default initialization using the number of available processor cores
ThreadPool threadPool = new ThreadPool();

// Custom initialization with a specific number of threads
ThreadPool threadPool = new ThreadPool(10);

