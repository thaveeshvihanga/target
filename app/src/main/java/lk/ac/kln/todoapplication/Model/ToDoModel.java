package lk.ac.kln.todoapplication.Model;

public class ToDoModel {
    private int id;
    private String task;
    private int status;
    private String description;
    private String dueDate;
    private String tags;
    private int userId;

    public ToDoModel() {}

    // Getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
