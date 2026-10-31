#!/bin/bash

echo "Welcome to Kafka Command Executor!"

# Function to list Kafka topics
list_topics() {
    kafka-topics --list --bootstrap-server $1
}

# Function to consume messages from a Kafka topic --from-beginning (dev-bilr-lms-action-tx)
consume_messages() {
    kafka-console-consumer --bootstrap-server $1 --topic $2 $3
}

# Function to produce messages to a Kafka topic
produce_messages() {
    kafka-console-producer --bootstrap-server $1 --topic $2
}

# Function to create a Kafka topic
create_topic() {
    kafka-topics --bootstrap-server $1 --create --topic $2
}

# Function to delete a Kafka topic
delete_topic() {
    kafka-topics --bootstrap-server $1 --delete --topic $2
}

# Function to delete all messages from a Kafka topic
delete_all_messages() {
    echo "Deleting all messages from topic: $2"
    kafka-topics --bootstrap-server $1 --delete --topic $2
    echo "Recreating topic: $2"
    kafka-topics --bootstrap-server $1 --create --topic $2
}

# Main function
main() {
    echo "Choose an action:"
    echo "1. List Kafka topics"
    echo "2. Consume messages from a Kafka topic"
    echo "3. Produce messages to a Kafka topic"
    echo "4. Create a Kafka topic"
    echo "5. Delete a Kafka topic"
    echo "6. Delete all messages from a Kafka topic"
    read -p "Enter your choice (1/2/3/4/5/6): " choice

    case $choice in
        1)
            list_topics localhost:9094
            ;;
        2)
            read -p "Enter the topic name: " topic
            read -p "Enter offset options if any (leave empty otherwise): " offset_options
            consume_messages localhost:9094 $topic $offset_options
            ;;
        3)
            read -p "Enter the topic name: " topic
            produce_messages localhost:9094 $topic
            ;;
        4)
            read -p "Enter the topic name: " topic
            create_topic localhost:9094 $topic
            ;;
        5)
            read -p "Enter the topic name to delete: " topic
            delete_topic localhost:9094 $topic
            ;;
        6)
            read -p "Enter the topic name to delete all messages: " topic
            delete_all_messages localhost:9094 $topic
            ;;
        *)
            echo "Invalid choice. Exiting."
            exit 1
            ;;
    esac
}

# Call the main function
main
