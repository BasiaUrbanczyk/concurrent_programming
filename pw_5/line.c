#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

#define NR_PROC 7

void line(int b, int pom){
    pid_t pid;
    int help = getpid();
    switch (pid = fork()) {
        case -1:
            perror("Error in fork\n");
            exit(1);

        case 0: /* proces potomny */
            printf("I am a child and my pid is %d and my parent's pid is %d\n", getpid(), help);
            pom++;
            if (b < NR_PROC) {
                line(b + 1, pom);
            }
            return;

        default: /* proces macierzysty */
            if (pom == 1) {
                printf("I am a parent and my pid is %d\n", getpid());
            }

            if (wait(0) == -1) {
                perror("Error in wait\n");
                exit(1);
            }
    }
}

int main() {
    line(1, 1);
    return 0;
}
