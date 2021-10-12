package taa.command;

import taa.storage.Storage;
import taa.assessment.Assessment;
import taa.exception.TaaException;
import taa.Ui;
import taa.assessment.AssessmentList;
import taa.module.Module;
import taa.module.ModuleList;
import taa.student.Student;
import taa.student.StudentList;

/**
 * Class that deals with the outputting of the average marks.
 */
public class AverageMarksCommand extends Command {
    private static final String KEY_MODULE_CODE = "c";
    private static final String KEY_ASSESSMENT_NAME = "a";
    private static final String[] ADD_ASSESSMENT_ARGUMENT_KEYS = {KEY_MODULE_CODE, KEY_ASSESSMENT_NAME};

    private static final String MESSAGE_FORMAT_AVERAGE_MARKS_USAGE = "Usage: %s "
            + "%s/<MODULE_CODE> %s/<ASSESSMENT_NAME>";
    private static final String MESSAGE_FORMAT_AVERAGE_MARKS = "Average marks for %s is %,.2f";
    private static final String MESSAGE_FORMAT_AVERAGE_MARKS_WITH_UNMARKED = "Average marks for %s is %,.2f\n"
            + "Note that %d student(s) have yet to be marked!";

    public AverageMarksCommand(String argument) {
        super(argument, ADD_ASSESSMENT_ARGUMENT_KEYS);
    }

    /**
     * Checks for errors before calling the function to output the average marks
     * of an assessment in a module.
     *
     * @param moduleList List of modules.
     * @param ui The ui instance to handle interactions with the user.
     * @param storage The storage instance to handle saving.
     * @throws TaaException When average marks command is invalid.
     */
    @Override
    public void execute(ModuleList moduleList, Ui ui, Storage storage) throws TaaException {
        if (argument.isEmpty()) {
            throw new TaaException(getUsageMessage());
        }

        if (!checkArguments()) {
            throw new TaaException(getMissingArgumentMessage());
        }

        String moduleCode = argumentMap.get(KEY_MODULE_CODE);
        Module module = moduleList.getModule(moduleCode);
        if (module == null) {
            throw new TaaException(MESSAGE_MODULE_NOT_FOUND);
        }

        StudentList studentList = module.getStudentList();
        if (studentList.getSize() <= 0) {
            throw new TaaException(MESSAGE_NO_STUDENTS);
        }

        AssessmentList assessmentList = module.getAssessmentList();
        Assessment assessment = assessmentList.getAssessment(argumentMap.get(KEY_ASSESSMENT_NAME));
        if (assessment == null) {
            throw new TaaException(MESSAGE_INVALID_ASSESSMENT_NAME);
        }

        printAverageMarks(ui, module, assessment);
    }

    /**
     * Outputs the average marks of an assessment in a module.
     *
     * @param ui The ui instance to handle interactions with the user.
     * @param module The module the assessment belongs to.
     */
    private void printAverageMarks(Ui ui, Module module, Assessment assessment) {
        StudentList studentList = module.getStudentList();
        String assessmentName = assessment.getName();

        double classSize = studentList.getSize();
        double totalMarks = 0;
        int unmarkedStudents = 0;
        for (Student student : studentList.getStudents()) {
            if (student.marksExist(assessmentName)) {
                totalMarks += student.getMarks(assessmentName);
            } else {
                unmarkedStudents += 1;
            }
        }

        double averageMarks = totalMarks / classSize;
        if (unmarkedStudents <= 0) {
            ui.printMessage(String.format(MESSAGE_FORMAT_AVERAGE_MARKS, assessmentName, averageMarks));
        } else {
            ui.printMessage(String.format(MESSAGE_FORMAT_AVERAGE_MARKS_WITH_UNMARKED,
                    assessmentName, averageMarks, unmarkedStudents));
        }
    }

    /**
     * Returns the usage message of the average marks command.
     *
     * @return String which contains the usage message.
     */
    @Override
    protected String getUsageMessage() {
        return String.format(
                MESSAGE_FORMAT_AVERAGE_MARKS_USAGE,
                COMMAND_AVERAGE_MARKS,
                KEY_MODULE_CODE,
                KEY_ASSESSMENT_NAME
        );
    }
}
