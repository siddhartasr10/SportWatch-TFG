#!/usr/bin sh

$(sudo systemctl start docker);
$(gnome-terminal --tab -- sh -c "cd SportWatch; mvn clean spring-boot:run");
$(gnome-terminal --tab -- sh -c "cd SportWatch_frontend; ng serve");
$(gnome-terminal --tab -- sh -c "cd db; docker compose up -d");
