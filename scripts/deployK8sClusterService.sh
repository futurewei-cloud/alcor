#!/bin/bash

#Clean up
kubectl delete svc alcor
kubectl delete deployment alcor

kubectl apply -f kubernetes/app/controller-deployment.yaml
kubectl get deployments -o wide

kubectl expose deployment alcor --type=LoadBalancer --name=alcor
kubectl get svc -o wide
kubectl get po -A
