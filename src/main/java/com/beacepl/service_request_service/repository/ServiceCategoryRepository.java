package com.beacepl.service_request_service.repository;

import com.beacepl.service_request_service.entity.ServiceCategory;
import com.beacepl.service_request_service.entity.ServiceRequestDefinition;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends MongoRepository<ServiceCategory, String> {

    @Query(value = "{ 'serviceRequestList.name': ?0 }")
    Optional<ServiceCategory> findCategoryByServiceName(String serviceName);

    default Optional<ServiceRequestDefinition> findServiceRequestByName(String serviceName) {
        return findCategoryByServiceName(serviceName)
                .flatMap(category -> category.getServiceRequestList() != null ? category.getServiceRequestList().stream()
                        .filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(serviceName))
                        .findFirst() : Optional.empty());
    }

    @Aggregation(pipeline = {
            "{ '$match': { 'active': true } }",
            "{ '$project': { " +
            "    'name': 1, 'description': 1, 'icon': 1, 'order': 1, 'active': 1, " +
            "    'serviceRequestList': { " +
            "        '$filter': { " +
            "            'input': '$serviceRequestList', " +
            "            'as': 'req', " +
            "            'cond': { '$eq': [ '$$req.active', true ] } " +
            "        } " +
            "    } " +
            "} }",
            "{ '$unwind': '$serviceRequestList' }",
            "{ '$sort': { 'serviceRequestList.order': -1 } }",
            "{ '$group': { " +
            "    '_id': '$_id', " +
            "    'name': { '$first': '$name' }, " +
            "    'description': { '$first': '$description' }, " +
            "    'icon': { '$first': '$icon' }, " +
            "    'order': { '$first': '$order' }, " +
            "    'active': { '$first': '$active' }, " +
            "    'serviceRequestList': { '$push': '$serviceRequestList' } " +
            "} }",
            "{ '$sort': { 'order': 1 } }"
    })
    List<ServiceCategory> findAllActiveServiceRequestsSorted();
}