#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("No arguments provided.\n");
        return 4;
    }
    int i;
    int fd[2];
    for (i = 1; i < argc - 1; i++) {
        if (pipe(fd) == -1) {
            printf("An error occured with opening the pipe.\n");
            return 1;
        }
        int id = fork();
        switch (id) {
            case -1:
                perror("Error in fork\n");
                exit(1);

            case 0: //child process
                if (close(fd[0]) == -1) {
                    return 2;
                }
                dup2(fd[1], STDOUT_FILENO);
                if (close(fd[1]) == -1) {
                    return 2;
                }
                execlp(argv[i], argv[i], NULL);
                return 5;

            default: //parent process
                wait(NULL);
                if (close(fd[1]) == -1) {
                    return 2;
                }
                dup2(fd[0], STDIN_FILENO);
                if (close(fd[0]) == -1) {
                    return 2;
                }
                break;
        }
    }
    execlp(argv[i], argv[i], NULL);
    return 0;
}




