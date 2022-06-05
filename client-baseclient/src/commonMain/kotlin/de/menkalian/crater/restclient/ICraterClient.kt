package de.menkalian.crater.restclient

import de.menkalian.crater.data.task.ChangeLog
import de.menkalian.crater.data.task.Task
import de.menkalian.crater.data.telemetrie.TelemetrieReport
import de.menkalian.crater.restclient.error.CraterException
import kotlinx.coroutines.Job

/**
 * User facing interface of the crater client library
 *
 * This provides an abstraction for calling the REST-API.
 */
@Suppress("unused")
interface ICraterClient {
    /**
     * Callback object for retrieving version information
     */
    fun interface VersionCallback {
        /**
         * Called when information is available.
         * **THIS IS NOT CALLED ON A SPECIFIC THREAD!**
         *
         * @param upToDate: Whether the current version is up to date (if no current version is known, this defaults to `true`)
         * @param currentUpstreamVersion: Determined version of the upstream (server side) database.
         */
        fun onVersion(upToDate: Boolean, currentUpstreamVersion: Long)
    }

    /**
     * Callback object for retrieving changes
     */
    fun interface ChangelogCallback {
        /**
         * Called when information is available.
         * **THIS IS NOT CALLED ON A SPECIFIC THREAD!**
         *
         * @param targetVersion: Version up to which changes could be retrieved
         * @param changes: List of incremental changes. You have to check the versions to apply them in the correct order.
         */
        fun onChanges(targetVersion: Long, changes: List<ChangeLog>)
    }

    /**
     * Callback object for retrieving a single task
     */
    fun interface TaskCallback {
        /**
         * Called when information is available.
         * **THIS IS NOT CALLED ON A SPECIFIC THREAD!**
         *
         * @param task: Received task object
         */
        fun onTask(task: Task)
    }

    /**
     * Callback object for retrieving a list of tasks
     */
    fun interface TaskListCallback {
        /**
         * Called when information is available.
         * **THIS IS NOT CALLED ON A SPECIFIC THREAD!**
         *
         * @param tasks: Received task objects
         */
        fun onTaskList(tasks: List<Task>)
    }

    /**
     * Callback object for performing an action
     */
    fun interface BooleanCallback {
        /**
         * Called when action was performed.
         * **THIS IS NOT CALLED ON A SPECIFIC THREAD!**
         *
         * @param success: Whether the action was successful or not
         */
        fun onCompleted(success: Boolean)
    }
    /**
     * Callback object for handling an error
     */
    fun interface ExceptionCallback {
        /**
         * Called when error occurs.
         * **THIS IS NOT CALLED ON A SPECIFIC THREAD!**
         *
         * @param exception: Exception that occured
         */
        fun onError(exception: CraterException)
    }

    private object NoopExceptionCallback : ExceptionCallback {
        override fun onError(exception: CraterException) {}
    }

    /**
     * Checks whether the current content version is up to date with the server.
     *
     * @param currentDatabaseVersion Local version of the database to check against
     * @param callback Callback to receive the information
     *
     * @return Coroutine [Job] where the action is performed
     *
     * @throws CraterException When there is a problem performing the action. Check [CraterException.error] to determine the source of the error.
     */
    fun checkUpToDate(currentDatabaseVersion: Long, callback: VersionCallback, onError: ExceptionCallback = NoopExceptionCallback): Job

    /**
     * Retrieves the upstream content version from the server.
     *
     * @param callback Callback to receive the information
     *
     * @return Coroutine [Job] where the action is performed
     *
     * @throws CraterException When there is a problem performing the action. Check [CraterException.error] to determine the source of the error.
     */
    fun getUpstreamVersion(callback: VersionCallback, onError: ExceptionCallback = NoopExceptionCallback): Job

    /**
     * Retrieves the patch information for upgrading the desired version range
     *
     * @param currentDatabaseVersion Current (start) version of the patch
     * @param targetVersion Optional target version of the patch. If set to `null` the most recent version is used.
     * @param callback Callback to receive the information
     *
     * @return Coroutine [Job] where the action is performed
     *
     * @throws CraterException When there is a problem performing the action. Check [CraterException.error] to determine the source of the error.
     */
    fun getPatchChangeLogs(currentDatabaseVersion: Long, targetVersion: Long?, callback: ChangelogCallback, onError: ExceptionCallback = NoopExceptionCallback): Job

    /**
     * Saves the given task to the database
     *
     * @param task Task to save
     * @param callback Callback to receive the information
     *
     * @return Coroutine [Job] where the action is performed
     *
     * @throws CraterException When there is a problem performing the action. Check [CraterException.error] to determine the source of the error.
     */
    fun saveTaskInUpstreamDatabase(task: Task, callback: TaskCallback, onError: ExceptionCallback = NoopExceptionCallback): Job

    /**
     * Queries the task with the given ID from the database
     *
     * @param id ID of the task
     * @param callback Callback to receive the information
     *
     * @return Coroutine [Job] where the action is performed
     *
     * @throws CraterException When there is a problem performing the action. Check [CraterException.error] to determine the source of the error.
     */
    fun getTask(id: Long, callback: TaskCallback, onError: ExceptionCallback = NoopExceptionCallback): Job

    /**
     * Queries all tasks from the database. Can be used to initialize the local database.
     *
     * @param callback Callback to receive the information
     *
     * @return Coroutine [Job] where the action is performed
     *
     * @throws CraterException When there is a problem performing the action. Check [CraterException.error] to determine the source of the error.
     */
    fun getAllTasks(callback: TaskListCallback, onError: ExceptionCallback = NoopExceptionCallback): Job

    /**
     * Remove the given task from the database
     *
     * @param id ID of the task
     * @param callback Callback to receive the information
     *
     * @return Coroutine [Job] where the action is performed
     *
     * @throws CraterException When there is a problem performing the action. Check [CraterException.error] to determine the source of the error.
     */
    fun removeTask(id: Long, callback: BooleanCallback, onError: ExceptionCallback = NoopExceptionCallback): Job

    /**
     * Upload a telemetrie report to the server
     *
     * @param report Report to upload
     * @param callback Callback to receive the information
     *
     * @return Coroutine [Job] where the action is performed
     *
     * @throws CraterException When there is a problem performing the action. Check [CraterException.error] to determine the source of the error.
     */
    fun uploadTelemetrieReport(report: TelemetrieReport, callback: BooleanCallback, onError: ExceptionCallback = NoopExceptionCallback): Job
}