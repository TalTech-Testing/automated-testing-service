#!/bin/bash -e

#  Text colors
RED='\033[0;31m'
GREEN='\033[0;32m'
GREEN_B='\033[1;32m'
CYAN='\033[0;36m'
CYAN_B='\033[1;36m'
YELLOW='\033[1;33m'
NC='\033[0m' #  No Color

#INSTALL GIT
if [ $(dpkg-query -W -f='${Status}' git 2>/dev/null | grep -c "ok installed") -eq 0 ];
then
  printf "${YELLOW}No git. Setting up git.${NC}\n"
  sudo apt --force-yes --yes install git
else
  printf "${GREEN}git already installed.${NC}\n"
fi

#CLONING GIT REPOSITORY
if [ -d .git ]; then
  printf "${GREEN}git repo already cloned${NC}\n"
elif [ ! -d "iti0203-2019-back" ]; then
  printf "${YELLOW}cloning git repo${NC}\n"
  git clone https://gitlab.cs.ttu.ee/envomp/automated-testing-service.git
  cd automated-testing-service
  printf "${GREEN}git repo cloned${NC}\n"
else
  printf "${GREEN}git repo already cloned${NC}\n"
  cd automated-testing-service
fi;

#INSTALL DOCKER
if [ $(dpkg-query -W -f='${Status}' docker 2>/dev/null | grep -c "ok installed") -eq 0 ];
then
  printf "${YELLOW}No docker. Setting up docker.${NC}\n"
  sudo apt --force-yes --yes install docker
else
  printf "${GREEN}docker already installed.${NC}\n"
fi

#INSTALL DOCKER-COMPOSE
if [ $(dpkg-query -W -f='${Status}' docker-compose 2>/dev/null | grep -c "ok installed") -eq 0 ];
then
  printf "${YELLOW}No docker-compose. Setting up docker-compose.${NC}\n"
  sudo apt --force-yes --yes install docker-compose
else
  printf "${GREEN}docker-compose already installed.${NC}\n"
fi

sudo docker-compose up -d
IP=$(sudo docker ps --filter "name=adminer" | grep -P "(\d.\d.\d.\d:\d+)" -o)
printf "${GREEN}Opening site at ${IP}.${NC}\n"
xdg-open "http://${IP}"
