package trungle95.personal.example.PPMT.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trungle95.personal.example.PPMT.domain.BackLog;
import trungle95.personal.example.PPMT.domain.Project;
import trungle95.personal.example.PPMT.domain.ProjectTask;
import trungle95.personal.example.PPMT.exceptions.ProjectNotFoundException;
import trungle95.personal.example.PPMT.repositories.BackLogRepository;
import trungle95.personal.example.PPMT.repositories.ProjectRepository;
import trungle95.personal.example.PPMT.repositories.ProjectTaskRepository;

import java.util.List;

@Service
public class  ProjectTaskService {

    @Autowired
    private BackLogRepository backLogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask){

        try {
            //PTs to be added to a specific project, project != null, BL exists
            BackLog backlog = backLogRepository.findByProjectIdentifier(projectIdentifier);
            //set the bl to pt
            projectTask.setBacklog(backlog);
            //we want our project sequence to be like this: IDPRO-1  IDPRO-2  ...100 101
            Integer BacklogSequence = backlog.getPTSequence();
            // Update the BL SEQUENCE
            BacklogSequence++;

            backlog.setPTSequence(BacklogSequence);

            //Add Sequence to Project Task
            projectTask.setProjectSequence(backlog.getProjectIdentifier()+"-"+BacklogSequence);
            projectTask.setProjectIdentifier(projectIdentifier);

            //INITIAL priority when priority null

            //INITIAL status when status is null
            if(projectTask.getStatus()==""|| projectTask.getStatus()==null){
                projectTask.setStatus("TO_DO");
            }

            if((projectTask.getPriority() == null) || (projectTask.getPriority() == 0)){ //In the future we need projectTask.getPriority()== 0 to handle the form
                projectTask.setPriority(3);
            }

            return projectTaskRepository.save(projectTask);
        }catch (Exception e){
            throw new ProjectNotFoundException("Project not Found");
        }

    }

    public Iterable<ProjectTask> findBacklogById(String id){

        Project project = projectRepository.findByProjectIdentifier(id);

        if(project==null){
            throw new ProjectNotFoundException("Project with ID: '" +id+ "' does not exist");
        }
        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
    }

    public ProjectTask findByProjectSequence(String backlog_id, String pt_id) {

        //make sure we are searching on an existing backlog
        BackLog backLog = backLogRepository.findByProjectIdentifier(backlog_id);
        if (backLog==null){
            throw new ProjectNotFoundException("Project with ID: '" +backlog_id+ "' does not exist");
        }

        //make sure that our task exists
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);

        if(projectTask == null) {
            throw new ProjectNotFoundException("Project Task '" +pt_id+ "' not found");
        }

        //make sure the backlog/project id in the path corresponds to the right project
        if(!projectTask.getProjectIdentifier().equals(backlog_id)){
            throw new ProjectNotFoundException("Project Task '" +pt_id+ "' does not exist in project: '" +backlog_id);
        }

        //make sure we are searching on the right backlog

        return projectTask;
    }

    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id){

        ProjectTask projectTask = findByProjectSequence(backlog_id, pt_id);

        projectTask = updatedTask;

        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence(String backlog_id, String pt_id){
        ProjectTask projectTask = findByProjectSequence(backlog_id,pt_id);
        projectTaskRepository.delete(projectTask);
    }
}
